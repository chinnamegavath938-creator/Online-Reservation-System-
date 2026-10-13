package com.reservation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.security.SecureRandom;

final class AuthService {
    private AuthService() {
    }

    static void registerCustomer(String username, char[] password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltText = Passwords.toHex(salt);
        String passwordHash = Passwords.hash(new String(password), salt);
        try (Connection connection = Database.connect();
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO users (username, password_salt, password_hash, role) VALUES (?, ?, ?, 'CUSTOMER')")) {
            insert.setString(1, username);
            insert.setString(2, saltText);
            insert.setString(3, passwordHash);
            insert.executeUpdate();
        }
    }

    static boolean authenticate(String username, char[] password, String requiredRole) throws Exception {
        String saltText;
        String storedHash;
        String role;
        try (Connection connection = Database.connect();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT password_salt, password_hash, role FROM users WHERE username = ?")) {
            query.setString(1, username);
            try (var result = query.executeQuery()) {
                if (!result.next()) {
                    return false;
                }
                saltText = result.getString("password_salt");
                storedHash = result.getString("password_hash");
                role = result.getString("role");
            }
        }

        byte[] salt = java.util.HexFormat.of().parseHex(saltText);
        String candidateHash = Passwords.hash(new String(password), salt);
        return requiredRole.equals(role) && Passwords.matches(candidateHash, storedHash);
    }
}
