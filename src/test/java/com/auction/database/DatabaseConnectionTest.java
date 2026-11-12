package com.auction.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

class DatabaseConnectionTest {

    @Test
    void opensSQLiteConnection() throws Exception {
        try (Connection connection = DatabaseConnection.getConnection()) {
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void enablesForeignKeysForEachConnection() throws Exception {
        try (Connection firstConnection = DatabaseConnection.getConnection();
             Connection secondConnection = DatabaseConnection.getConnection()) {
            try (Statement statement = firstConnection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt(1));
            }

            try (Statement statement = secondConnection.createStatement();
                 ResultSet resultSet = statement.executeQuery("PRAGMA foreign_keys")) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt(1));
            }
        }
    }

    @Test
    void returnsSeparateConnectionObjects() throws Exception {
        try (Connection first = DatabaseConnection.getConnection();
             Connection second = DatabaseConnection.getConnection()) {
            assertNotSame(first, second);
        }
    }
}
