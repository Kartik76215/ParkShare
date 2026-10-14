package com.parkshare.service;

import com.parkshare.exception.BadRequestException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private static final String PREFIX = "pbkdf2";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String hash(String plainPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plainPassword, salt, ITERATIONS);
        return PREFIX + ":" + ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt)
                + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public boolean matches(String plainPassword, String storedPassword) {
        if (storedPassword == null || plainPassword == null) {
            return false;
        }
        if (!storedPassword.startsWith(PREFIX + ":")) {
            return storedPassword.equals(plainPassword);
        }
        String[] parts = storedPassword.split(":");
        if (parts.length != 4) {
            return false;
        }
        int iterations = Integer.parseInt(parts[1]);
        byte[] salt = Base64.getDecoder().decode(parts[2]);
        byte[] expected = Base64.getDecoder().decode(parts[3]);
        byte[] actual = pbkdf2(plainPassword, salt, iterations);
        return constantTimeEquals(expected, actual);
    }

    public boolean isHashed(String storedPassword) {
        return storedPassword != null && storedPassword.startsWith(PREFIX + ":");
    }

    private byte[] pbkdf2(String plainPassword, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new BadRequestException("Password hashing failed.");
        }
    }

    private boolean constantTimeEquals(byte[] first, byte[] second) {
        if (first.length != second.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < first.length; i++) {
            result |= first[i] ^ second[i];
        }
        return result == 0;
    }
}
