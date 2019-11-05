package net.cowpi.crypto;

import dagger.Module;
import dagger.Provides;

import javax.crypto.Cipher;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Module
public class CryptoModule {

    @Provides
    public static MessageDigest messageDigest(){
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    public static Cipher aesGcmCipher(){
        try {
            return Cipher.getInstance("AES/GCM/NoPadding");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
