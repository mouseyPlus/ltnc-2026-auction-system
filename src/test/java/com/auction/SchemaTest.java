package com.auction;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchemaTest {

    private static final String SCHEMA_RESOURCE = "/db/schema.sql";

    @Test
    void createsExpectedTablesIndexesAndEnablesForeignKeys() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            executeSchema(connection);

            assertEquals(
                    Set.of("USERS", "USER_ROLES", "ITEMS", "AUCTIONS", "BIDS"),
                    objectNames(connection, "table")
            );

            assertTrue(objectNames(connection, "index").containsAll(Set.of(
                    "ux_auctions_active_item",
                    "idx_auctions_item_id",
                    "idx_items_owner_id",
                    "idx_auctions_status_start_time",
                    "idx_bids_auction_time",
                    "idx_bids_bidder_time",
                    "idx_bids_auction_amount"
            )));

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt(1));
            }
        }
    }

    @Test
    void foreignKeyRejectsRoleForNonexistentUser() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            executeSchema(connection);

            assertThrows(SQLException.class, () -> executeUpdate(connection,
                    "INSERT INTO USER_ROLES (user_id, role) VALUES (999, 'BIDDER')"));
        }
    }

    @Test
    void activeAuctionIsUniquePerItem() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            executeSchema(connection);
            insertValidUser(connection, 1);
            insertValidItem(connection, 1, 1);
            insertAuction(connection, 1, 1, "OPEN");

            assertThrows(SQLException.class, () -> insertAuction(connection, 2, 1, "RUNNING"));
        }
    }

    @Test
    void historicalAuctionAllowsNewActiveAuction() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:")) {
            executeSchema(connection);
            insertValidUser(connection, 1);
            insertValidItem(connection, 1, 1);
            insertAuction(connection, 1, 1, "FINISHED");

            assertDoesNotThrow(() -> insertAuction(connection, 2, 1, "OPEN"));
        }
    }

    private void insertValidUser(Connection connection, int userId) throws SQLException {
        executeUpdate(connection, """
                INSERT INTO USERS (
                    user_id, username, email, password_hash, display_name, created_at_utc
                ) VALUES (%d, 'user%d', 'user%d@example.com', 'hash', 'User %d', '2026-01-01T00:00:00Z')
                """.formatted(userId, userId, userId, userId));
    }

    private void insertValidItem(Connection connection, int itemId, int ownerId) throws SQLException {
        executeUpdate(connection, """
                INSERT INTO ITEMS (
                    item_id, owner_id, name, category, created_at_utc
                ) VALUES (%d, %d, 'Item %d', 'ART', '2026-01-01T00:00:00Z')
                """.formatted(itemId, ownerId, itemId));
    }

    private void insertAuction(Connection connection, int auctionId, int itemId, String status)
            throws SQLException {
        executeUpdate(connection, """
                INSERT INTO AUCTIONS (
                    auction_id, item_id, seller_id, starting_price_vnd,
                    start_time_utc, end_time_utc, status
                ) VALUES (%d, %d, 1, 100, '2026-01-02T00:00:00Z', '2026-01-03T00:00:00Z', '%s')
                """.formatted(auctionId, itemId, status));
    }

    private void executeUpdate(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void executeSchema(Connection connection) throws IOException, SQLException {
        try (InputStream input = getClass().getResourceAsStream(SCHEMA_RESOURCE)) {
            if (input == null) {
                throw new IOException("Schema resource not found: " + SCHEMA_RESOURCE);
            }

            String schema = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(schema);
            }
        }
    }

    private Set<String> objectNames(Connection connection, String type) throws SQLException {
        String query = """
                SELECT name
                FROM sqlite_master
                WHERE type = ?
                AND name NOT GLOB 'sqlite_*'
                """;

        try (var statement = connection.prepareStatement(query)) {
            statement.setString(1, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                Set<String> names = new HashSet<>();
                while (resultSet.next()) {
                    names.add(resultSet.getString("name"));
                }
                return names;
            }
        }
    }
}
