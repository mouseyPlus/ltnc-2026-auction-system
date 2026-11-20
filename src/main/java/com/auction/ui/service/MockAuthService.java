package com.auction.ui.service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class MockAuthService {

    private static final Map<String, Account> accounts = new HashMap<>();
    private static final SecureRandom random = new SecureRandom();

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{8,}$"
    );

    private record Account(
            String username,
            String phone,
            String email,
            byte[] salt,
            byte[] passwordHash
    ) {}

    private MockAuthService() {}

    public static String register(
            String username, String phone,
            String email, String password) {

        username = username.trim();
        phone = phone.trim();
        email = email.trim();

        if (username.isEmpty() || phone.isEmpty()
                || email.isEmpty() || password.isEmpty()) {
            return "Please fill in all fields.";
        }

        if (!username.matches("[A-Za-z0-9._]{3,30}")) {
            return "Username must be 3-30 letters, numbers, dots and underscores.";
        }

        if (!phone.matches("0[0-9]{9,10}")) {
            return "Phone number must start with 0 and contain 10 or 11 digits.";
        }

        if (!EMAIL.matcher(email).matches()) {
            return "Invalid email address.";
        }

        if (!PASSWORD.matcher(password).matches()) {
            return "Password needs 8+ characters, uppercase, lowercase, number and symbol.";
        }

        if (accounts.containsKey(username.toLowerCase())) {
            return "Username already exists.";
        }

        for (Account account : accounts.values()) {
            if (account.phone().equals(phone)) {
                return "Phone number already registered.";
            }
            if (account.email().equalsIgnoreCase(email)) {
                return "Email already registered.";
            }
        }

        byte[] salt = new byte[16];
        random.nextBytes(salt);

        accounts.put(username.toLowerCase(),
                new Account(username, phone, email,
                        salt, hash(password, salt)));

        return null;
    }

    public static String login(String identifier, String password) {
        identifier = identifier.trim();

        for (Account account : accounts.values()) {
            if (account.username().equalsIgnoreCase(identifier)
                    || account.phone().equals(identifier)
                    || account.email().equalsIgnoreCase(identifier)) {

                byte[] attempt = hash(password, account.salt());
                boolean valid = java.security.MessageDigest.isEqual(
                        attempt, account.passwordHash()
                );
                Arrays.fill(attempt, (byte) 0);

                return valid ? account.username() : null;
            }
        }
        return null;
    }

    private static byte[] hash(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, 120000, 256
        );

        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        } finally {
            spec.clearPassword();
        }
    }
}
