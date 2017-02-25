package com.notecrypt.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Cryptox {

    private static final int IV_SIZE = 16;
    //for encrypt with AES 128 bit
    private static final int KEY_SIZE = 128;
    private static Cryptox singleton = new Cryptox();

    private Cryptox() {

    }

    /**
     * Returns the singleton.
     *
     * @return singleton of Crypto
     */
    public static Cryptox getInstance() {
        return singleton;
    }

    /**
     * Generate a random initialization vector.
     *
     * @return random generated IV
     */
    byte[] generateIv() {
        final SecureRandom random = new SecureRandom();
        final byte[] ivBytes = new byte[IV_SIZE];
        random.nextBytes(ivBytes);
        return ivBytes;
    }

    byte[] deriveKey(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec specs = new PBEKeySpec(password.toCharArray(), salt, 1024, KEY_SIZE);
        SecretKey key = kf.generateSecret(specs);
        return key.getEncoded();
    }

}