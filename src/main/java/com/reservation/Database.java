package com.reservation;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

final class Database {
    private static final String URL = "jdbc:sqlite:reservation.db";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Database() {
    }

    static Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    static void initialize() throws Exception {
        try (Connection connection = connect();
             var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        username TEXT PRIMARY KEY,
                        password_salt TEXT NOT NULL,
                        password_hash TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'CUSTOMER'
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reservations (
                        pnr TEXT PRIMARY KEY,
                        owner_username TEXT NOT NULL DEFAULT '',
                        passenger_name TEXT NOT NULL,
                        mobile_number TEXT NOT NULL DEFAULT '',
                        flight_number TEXT NOT NULL DEFAULT '',
                        flight_name TEXT NOT NULL DEFAULT '',
                        class_type TEXT NOT NULL,
                        journey_date TEXT NOT NULL,
                        source_station TEXT NOT NULL,
                        destination_station TEXT NOT NULL,
                        seat_number TEXT NOT NULL DEFAULT '',
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
        }

        ensureUserRoleColumn();
        migrateLegacyTrainTable();
        ensureColumn("owner_username", "TEXT NOT NULL DEFAULT ''");
        ensureColumn("mobile_number", "TEXT NOT NULL DEFAULT ''");
        ensureColumn("flight_number", "TEXT NOT NULL DEFAULT ''");
        ensureColumn("flight_name", "TEXT NOT NULL DEFAULT ''");
        ensureColumn("seat_number", "TEXT NOT NULL DEFAULT ''");
        try (Connection connection = connect(); var statement = connection.createStatement()) {
            statement.execute("CREATE UNIQUE INDEX IF NOT EXISTS reservation_seat_unique "
                    + "ON reservations(flight_number, journey_date, class_type, seat_number) WHERE seat_number <> ''");
        }

        ensureDemoUser();
    }

    private static void migrateLegacyTrainTable() throws SQLException {
        boolean legacy = false;
        try (Connection connection = connect(); var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(reservations)")) {
            while (result.next()) if ("train_number".equals(result.getString("name"))) legacy = true;
        }
        if (!legacy) return;
        try (Connection connection = connect(); var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE reservations_flight_new (
                        pnr TEXT PRIMARY KEY, owner_username TEXT NOT NULL DEFAULT 'admin', passenger_name TEXT NOT NULL,
                        mobile_number TEXT NOT NULL DEFAULT '', flight_number TEXT NOT NULL DEFAULT '',
                        flight_name TEXT NOT NULL DEFAULT '', class_type TEXT NOT NULL,
                        journey_date TEXT NOT NULL, source_station TEXT NOT NULL,
                        destination_station TEXT NOT NULL, seat_number TEXT NOT NULL DEFAULT '',
                        created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)
                    """);
            statement.executeUpdate("INSERT INTO reservations_flight_new "
                    + "(pnr, owner_username, passenger_name, flight_number, flight_name, class_type, journey_date, "
                    + "source_station, destination_station, created_at) "
                    + "SELECT pnr, 'admin', passenger_name, train_number, train_name, class_type, journey_date, "
                    + "source_station, destination_station, created_at FROM reservations");
            statement.execute("DROP TABLE reservations");
            statement.execute("ALTER TABLE reservations_flight_new RENAME TO reservations");
        }
    }

    private static void ensureColumn(String column, String definition) throws SQLException {
        try (Connection connection = connect(); var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(reservations)")) {
            while (result.next()) {
                if (column.equals(result.getString("name"))) return;
            }
        }
        try (Connection connection = connect(); var statement = connection.createStatement()) {
            statement.execute("ALTER TABLE reservations ADD COLUMN " + column + " " + definition);
        }
    }

    private static void ensureUserRoleColumn() throws SQLException {
        try (Connection connection = connect(); var statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA table_info(users)")) {
            while (result.next()) if ("role".equals(result.getString("name"))) return;
        }
        try (Connection connection = connect(); var statement = connection.createStatement()) {
            statement.execute("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'CUSTOMER'");
        }
    }

    private static void ensureDemoUser() throws Exception {
        ensureUser("admin", "admin123", "ADMIN");
        ensureUser("customer", "customer123", "CUSTOMER");
    }

    private static void ensureUser(String username, String password, String role) throws Exception {
        try (Connection connection = connect();
             PreparedStatement exists = connection.prepareStatement(
                     "SELECT 1 FROM users WHERE username = ?")) {
            exists.setString(1, username);
            try (var result = exists.executeQuery()) {
                if (result.next()) {
                    try (PreparedStatement update = connection.prepareStatement("UPDATE users SET role=? WHERE username=?")) {
                        update.setString(1, role); update.setString(2, username); update.executeUpdate();
                    }
                    return;
                }
            }
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        String saltText = Passwords.toHex(salt);
        String passwordHash = Passwords.hash(password, salt);
        try (Connection connection = connect();
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT OR IGNORE INTO users (username, password_salt, password_hash, role) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, username);
            insert.setString(2, saltText);
            insert.setString(3, passwordHash);
            insert.setString(4, role);
            insert.executeUpdate();
        }
    }
}
