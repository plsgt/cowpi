package net.cowpi.android.perf;

import com.google.protobuf.ByteString;

import net.cowpi.crypto.CryptoEngine;
import net.cowpi.crypto.EphemeralKeyPair;
import net.cowpi.crypto.EphemeralPublicKey;
import net.cowpi.crypto.KeyRatchet;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.MessageProto.CowpiMessage.MessageType;
import net.cowpi.protobuf.MessageProto.OesCiphertext;
import net.cowpi.protobuf.MessageProto.OesPlaintext;
import net.cowpi.protobuf.MessageProto.ParticipantData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class SetupSessionCallable implements Callable<CowpiMessage> {


    private final CryptoEngine crypto;

    private long conversationId;
    private CowpiUser cowpiUser;
    private List<CowpiUser> otherUsers;
    private Map<String, KeyRatchet> oesRatchets;

    @Inject
    SetupSessionCallable(CryptoEngine crypto){
        this.crypto = crypto;
    }

    void init(long conversationId, CowpiUser cowpiUser, List<CowpiUser> otherUsers, Map<String, KeyRatchet> oesRatchets) {
        this.conversationId = conversationId;
        this.cowpiUser = cowpiUser;
        this.otherUsers = otherUsers;
        this.oesRatchets = oesRatchets;
    }

    @Override
    public CowpiMessage call() throws Exception {

        CowpiMessage.Builder builder = CowpiMessage.newBuilder()
                .setAuthor(cowpiUser.getName())
                .setType(MessageType.SETUP)
                .setConversationId(conversationId)
                .addParticipant(cowpiUser.getName());

        for(CowpiUser other: otherUsers){
            builder.addParticipant(other.getName());
        }

        byte[] authBytes = crypto.messageToAuthBytes(builder.build());

        for(CowpiUser other: otherUsers){

            KeyRatchet keyRatchet = crypto.generateKeyRatchet(cowpiUser.getName(), cowpiUser.getLongtermKeyPair(),
                    other.getName(), other.getLongtermKeyPair().getPublicKey());

            EphemeralPublicKey epk = new EphemeralPublicKey(other.getEphemeralKeyPair().getId(), other.getEphemeralKeyPair().getPublicKey());
            keyRatchet.setNextEphPublicKey(epk);

            EphemeralKeyPair ekp = keyRatchet.nextEphemeralKeyPair();

            byte[] encKey = keyRatchet.getNaxosEncryptionKey();

            EphemeralKeyPair nextEkp = keyRatchet.nextEphemeralKeyPair();

            byte[] ciphertext = crypto.encrypt(encKey, nextEkp.getPublicKey(), authBytes);

            ParticipantData participantData = ParticipantData.newBuilder()
                    .setKeyId(keyRatchet.getLastPublicKey().getId())
                    .setEphKey(ByteString.copyFrom(ekp.getPublicKey()))
                    .setCiphertext(ByteString.copyFrom(ciphertext))
                    .build();

            builder.putParticipantData(other.getName(), participantData);
        }

        for(Map.Entry<String, KeyRatchet> ratchetEntry: oesRatchets.entrySet()) {

            KeyRatchet ratchet = ratchetEntry.getValue();

            EphemeralKeyPair epk = ratchet.nextEphemeralKeyPair();

            byte[] encKey = ratchet.getNaxosEncryptionKey();

            EphemeralKeyPair nextKeyPair = ratchet.nextEphemeralKeyPair();

            OesPlaintext plaintext = OesPlaintext.newBuilder()
                    .setKeyId(nextKeyPair.getId())
                    .setEphKey(ByteString.copyFrom(nextKeyPair.getPublicKey()))
                    .build();

            byte[] ciphertext = crypto.encrypt(encKey, plaintext.toByteArray(), authBytes);

            OesCiphertext oesCiphertext = OesCiphertext.newBuilder()
                    .setLongtermKey(ByteString.copyFrom(cowpiUser.getLongtermKeyPair().getPublicKey()))
                    .setKeyId(ratchet.getLastPublicKey().getId())
                    .setEphKey(ByteString.copyFrom(epk.getPublicKey()))
                    .setCiphertext(ByteString.copyFrom(ciphertext))
                    .build();

            builder.putOesData(ratchetEntry.getKey(), oesCiphertext);
        }

        return builder.build();
    }
}
