package com.auction.database;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DatabaseInitializerTest {

    @Test
    void initializationCreatesExpectedTables() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseInitializer.initialize(connection);
            assertEquals(
                    Set.of("USERS", "USER_ROLES", "ITEMS", "AUCTIONS", "BIDS"),
                    objectNames(connection)
            );
        }
    }

    @Test
    void initializationCanBeCalledMultipleTimes() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseInitializer.initialize(connection);

            assertDoesNotThrow(() -> DatabaseInitializer.initialize(connection));
        }
    }

    @Test
    void initializationDoesNotDeleteExistingData() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            DatabaseInitializer.initialize(connection);

            try (var statement = connection.prepareStatement("""
                    INSERT INTO USERS (
                        user_id, username, email, phone, password_hash, display_name, created_at_utc
                    ) VALUES (1, 'user1', 'user1@example.com', '0900000001', 'hash', 'Test User',
                              '2026-01-01T00:00:00Z')
                    """)) {
                statement.executeUpdate();
            }

            DatabaseInitializer.initialize(connection);

            try (var statement = connection.prepareStatement(
                    "SELECT username FROM USERS WHERE user_id = 1");
                 ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("user1", resultSet.getString(1));
            }
        }
    }

    private Set<String> objectNames(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("""
                     SELECT name
                     FROM sqlite_master
                     WHERE type = 'table'
                     AND name NOT GLOB 'sqlite_*'
                     """)) {
            Set<String> names = new HashSet<>();
            while (resultSet.next()) {
                names.add(resultSet.getString("name"));
            }
            return names;
        }
    }
}
