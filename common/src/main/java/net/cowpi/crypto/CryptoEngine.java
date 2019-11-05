package net.cowpi.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.cowpi.protobuf.MessageProto.CowpiMessage;
import net.cowpi.protobuf.MessageProto.ParticipantData;
import org.bouncycastle.math.ec.rfc7748.X25519;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

@Singleton
public class CryptoEngine {
    private static final Logger logger = LoggerFactory.getLogger(CryptoEngine.class);

    private final Provider<MessageDigest> messageDigestProvider;
    private final Provider<Cipher> cipherProvider;
    private final SecureRandom random = new SecureRandom();

    @Inject
    public CryptoEngine(Provider<MessageDigest> messageDigestProvider, Provider<Cipher> cipherProvider) {
        X25519.precompute();
        this.messageDigestProvider = messageDigestProvider;
        this.cipherProvider = cipherProvider;
    }


    public KeyPair generateLongtermKeyPair(){
        byte[] priv = new byte[X25519.SCALAR_SIZE];
        X25519.generatePrivateKey(random, priv);


        byte[] pub = new byte[X25519.SCALAR_SIZE];
        X25519.generatePublicKey(priv, 0 , pub, 0);
        return new KeyPair(priv, pub);
    }

    public EphemeralKeyPair generateEphemeralKeyPair(long id, KeyPair keyPair){
        byte[] privkey = new byte[X25519.SCALAR_SIZE];
        random.nextBytes(privkey);
        return computeEphemeralKeyPair(id, keyPair, privkey);
    }

    private void toPrivateKey(byte[] input){
        input[0] &= 0xF8;
        input[X25519.SCALAR_SIZE - 1] &= 0x7F;
        input[X25519.SCALAR_SIZE - 1] |= 0x40;
    }

    public EphemeralKeyPair computeEphemeralKeyPair(long id, KeyPair keyPair, byte[] privkey){
        MessageDigest digest = messageDigestProvider.get();
        digest.update(privkey);
        byte[] input = digest.digest(keyPair.getPrivateKey());
        toPrivateKey(input);

        byte[] pub = new byte[X25519.SCALAR_SIZE];
        X25519.generatePublicKey(input, 0, pub, 0);

        return new EphemeralKeyPair(id, privkey, pub);
    }

    public KeyRatchet generateKeyRatchet(String localName, KeyPair longtermKeyPair, String remoteName, byte[] longtermPublicKey) {
        return new KeyRatchet(localName, longtermKeyPair, remoteName, longtermPublicKey, this);
    }

    public byte[] getNaxosDecryptionKey(String local, KeyPair longtermKeyPair, EphemeralKeyPair ephemeralKeyPair,
                                        String remote, byte[] longtermPublicKey, byte[] ephemeralPublicKey){
        MessageDigest digest = messageDigestProvider.get();
        digest.update(ephemeralKeyPair.getPrivateKey());
        byte[] ephPriv = digest.digest(longtermKeyPair.getPrivateKey());
        toPrivateKey(ephPriv);

        digest.reset();

        byte[] sharedSecret = new byte[X25519.SCALAR_SIZE];
        X25519.calculateAgreement(ephPriv, 0, longtermPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        X25519.calculateAgreement(longtermKeyPair.getPrivateKey(), 0, ephemeralPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        X25519.calculateAgreement(ephPriv, 0, ephemeralPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        digest.update(remote.getBytes());
        digest.update(local.getBytes());

        return digest.digest();
    }

    public byte[] getNaxosEncryptionKey(String local, KeyPair longtermKeyPair, EphemeralKeyPair ephemeralKeyPair,
                                        String remote, byte[] longtermPublicKey, byte[] ephemeralPublicKey){
        MessageDigest digest = messageDigestProvider.get();
        digest.update(ephemeralKeyPair.getPrivateKey());
        byte[] ephPriv = digest.digest(longtermKeyPair.getPrivateKey());
        toPrivateKey(ephPriv);

        digest.reset();

        byte[] sharedSecret = new byte[X25519.SCALAR_SIZE];
        X25519.calculateAgreement(longtermKeyPair.getPrivateKey(), 0, ephemeralPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        X25519.calculateAgreement(ephPriv, 0, longtermPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        X25519.calculateAgreement(ephPriv, 0, ephemeralPublicKey, 0, sharedSecret, 0);
        digest.update(sharedSecret);

        digest.update(local.getBytes());
        digest.update(remote.getBytes());

        return digest.digest();
    }

    public byte[] decrypt(byte[] key, byte[] ciphertext, byte[] associatedData) throws InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = cipherProvider.get();

        GCMParameterSpec spec = new GCMParameterSpec(128, new byte[16]);

        byte[] tmp = Arrays.copyOfRange(key, 0, 16);

        Key secret = new SecretKeySpec(tmp, "AES");

        cipher.init(Cipher.DECRYPT_MODE, secret, spec);
        cipher.updateAAD(associatedData);
        return cipher.doFinal(ciphertext);
    }

    public byte[] decrypt(byte[] key, byte[] ciphertext) throws InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = cipherProvider.get();

        GCMParameterSpec spec = new GCMParameterSpec(128, new byte[16]);

        byte[] tmp = Arrays.copyOfRange(key, 0, 16);

        Key secret = new SecretKeySpec(tmp, "AES");

        cipher.init(Cipher.DECRYPT_MODE, secret, spec);
        return cipher.doFinal(ciphertext);
    }

    public byte[] encrypt(byte[] key, byte[] plaintext, byte[] associatedData) throws InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = cipherProvider.get();

        GCMParameterSpec spec = new GCMParameterSpec(128, new byte[16]);

        byte[] tmp = Arrays.copyOfRange(key, 0, 16);

        Key secret = new SecretKeySpec(tmp, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secret, spec);
        cipher.updateAAD(associatedData);
        return cipher.doFinal(plaintext);
    }

    public byte[] encrypt(byte[] key, byte[] plaintext) throws InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = cipherProvider.get();

        GCMParameterSpec spec = new GCMParameterSpec(128, new byte[16]);

        byte[] tmp = Arrays.copyOfRange(key, 0, 16);

        Key secret = new SecretKeySpec(tmp, "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secret, spec);
        return cipher.doFinal(plaintext);
    }

    public byte[] messageToAuthBytesOes(CowpiMessage message, String receiver){
        ByteBuf buff = Unpooled.buffer();
        buff.writeLong(message.getConversationId());
        buff.writeInt(message.getType().getNumber());
        buff.writeCharSequence(message.getAuthor(), Charset.defaultCharset());
        buff.writeLong(message.getIndex());

        switch (message.getType()){
            case SETUP:
            case PARTICIPANT_UPDATE:
                List<String> users = new ArrayList<>(message.getParticipantList());
                Collections.sort(users);
                for(String user: users){
                    buff.writeCharSequence(user, Charset.defaultCharset());
                }
                break;
            case RECIEPT:
                buff.writeLong(message.getPrevIndex());
                break;
            case CONVERSATION:
                buff.writeBytes(message.getCiphertext().toByteArray());
                break;
        }

        if (receiver.equals(message.getAuthor())) {
            List<Map.Entry<String, ParticipantData>> data = new ArrayList<>(message.getParticipantDataMap().entrySet());
            Collections.sort(data, (d1, d2) -> d1.getKey().compareTo(d2.getKey()));
            for (Map.Entry<String, ParticipantData> tmp : data) {
                buff.writeCharSequence(tmp.getKey(), StandardCharsets.UTF_8);
                buff.writeBytes(tmp.getValue().toByteArray());
            }
        }
        else{
            ParticipantData data = message.getParticipantDataMap().get(receiver);
            buff.writeBytes(data.toByteArray());
        }

        return buff.slice().array();
    }

    public byte[] messageToAuthBytes(CowpiMessage message){
        ByteBuf buff = Unpooled.buffer();
        buff.writeLong(message.getConversationId());
        buff.writeInt(message.getType().getNumber());
        buff.writeCharSequence(message.getAuthor(), StandardCharsets.UTF_8);
        buff.writeLong(message.getIndex());

        switch (message.getType()){
            case SETUP:
            case PARTICIPANT_UPDATE:
                List<String> users = new ArrayList<>(message.getParticipantList());
                Collections.sort(users);
                for(String user: users){
                    buff.writeCharSequence(user, StandardCharsets.UTF_8);
                }
                break;
            case RECIEPT:
                buff.writeLong(message.getPrevIndex());
                break;
            case CONVERSATION:
                buff.writeBytes(message.getCiphertext().toByteArray());
                break;
        }

        return buff.slice().array();
    }
}
