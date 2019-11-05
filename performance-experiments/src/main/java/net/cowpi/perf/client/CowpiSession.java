package net.cowpi.perf.client;

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
import net.cowpi.protobuf.OesProto.OesPreKey;
import net.cowpi.server.config.OesPublicKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.inject.Inject;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CowpiSession {
    private static final Logger logger = LoggerFactory.getLogger(CowpiSession.class);

    private final Map<String, byte[]> oesPublicKeys;

    private final CryptoEngine crypto;
    private final Map<String, KeyRatchet> participantRatchets = new HashMap<>();
    private final Map<String, KeyRatchet> oesRatchets = new HashMap<>();

    private User localUser;
    private long conversationId;

    @Inject
    public CowpiSession(@OesPublicKeys Map<String, byte[]> oesPublicKeys, CryptoEngine crypto) {
        this.oesPublicKeys = oesPublicKeys;
        this.crypto = crypto;
    }

    public void init(User user, long conversationId, List<User> participants, Map<String, OesPreKey> oesPreKeys) {
        this.localUser = user;
        this.conversationId = conversationId;

        for(User participant: participants){
            KeyRatchet keyRatchet = crypto.generateKeyRatchet(localUser.getName(), localUser.getKeyPair(),
                    participant.getName(), participant.getKeyPair().getPublicKey());

            EphemeralKeyPair prekey = participant.nextEkp();

            EphemeralPublicKey epk = new EphemeralPublicKey(prekey.getId(), prekey.getPublicKey());
            keyRatchet.setNextEphPublicKey(epk);

            participantRatchets.put(participant.getName(), keyRatchet);
        }

        for(Map.Entry<String, byte[]> oes: oesPublicKeys.entrySet()){
            KeyRatchet keyRatchet = crypto.generateKeyRatchet(localUser.getName(),
                    localUser.getKeyPair(), oes.getKey(), oes.getValue());

            OesPreKey prekey = oesPreKeys.get(oes.getKey());

            EphemeralPublicKey epk = new EphemeralPublicKey(prekey.getKeyId(), prekey.getPublicKey().toByteArray());
            keyRatchet.setNextEphPublicKey(epk);

            oesRatchets.put(oes.getKey(), keyRatchet);
        }
    }

    public CowpiMessage setup() throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {

        CowpiMessage.Builder setup = CowpiMessage.newBuilder()
                .setAuthor(localUser.getName())
                .setConversationId(conversationId)
                .setType(MessageType.SETUP)
                .addParticipant(localUser.getName())
                .addAllParticipant(participantRatchets.keySet());

        byte[] authBytes = crypto.messageToAuthBytes(setup.build());

        for(Map.Entry<String, KeyRatchet> ratchetEntry: participantRatchets.entrySet()) {

            KeyRatchet ratchet = ratchetEntry.getValue();

            EphemeralKeyPair epk = ratchet.nextEphemeralKeyPair();

            byte[] encKey = ratchet.getNaxosEncryptionKey();

            EphemeralKeyPair nextKeyPair = ratchet.nextEphemeralKeyPair();

            byte[] ciphertext = crypto.encrypt(encKey, nextKeyPair.getPublicKey(), authBytes);

            ParticipantData participantData = ParticipantData.newBuilder()
                    .setKeyId(ratchet.getLastPublicKey().getId())
                    .setEphKey(ByteString.copyFrom(epk.getPublicKey()))
                    .setCiphertext(ByteString.copyFrom(ciphertext))
                    .build();

            setup.putParticipantData(ratchetEntry.getKey(), participantData);
        }

        authBytes = crypto.messageToAuthBytesOes(setup.build(), localUser.getName());

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
                    .setLongtermKey(ByteString.copyFrom(localUser.getKeyPair().getPublicKey()))
                    .setKeyId(ratchet.getLastPublicKey().getId())
                    .setEphKey(ByteString.copyFrom(epk.getPublicKey()))
                    .setCiphertext(ByteString.copyFrom(ciphertext))
                    .build();

            setup.putOesData(ratchetEntry.getKey(), oesCiphertext);
        }

        return setup.build();
    }

    public void init(User user, CowpiMessage message) {
        localUser = user;
        conversationId = message.getConversationId();

        for(Map.Entry<String, OesCiphertext> entry: message.getOesDataMap().entrySet()){

        }
    }
}
