package com.reservation;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.HexFormat;

final class Passwords {
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    private Passwords() {
    }

    static String hash(String password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            byte[] derivedKey = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return toHex(derivedKey);
        } finally {
            spec.clearPassword();
        }
    }

    static String toHex(byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    static boolean matches(String actual, String expected) {
        return MessageDigest.isEqual(
                HexFormat.of().parseHex(actual),
                HexFormat.of().parseHex(expected));
    }
}
