package com.auction.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseTestSupportTest {

  @Test
  void sharesDataAcrossConnectionsAndEnablesForeignKeys(@TempDir Path temporaryDirectory)
      throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection firstConnection = support.openConnection();
        Connection secondConnection = support.openConnection();
        Statement firstStatement = firstConnection.createStatement();
        Statement secondStatement = secondConnection.createStatement()) {
      firstStatement.execute(
          "CREATE TABLE test_support_values (value TEXT NOT NULL)");
      firstStatement.execute(
          "INSERT INTO test_support_values (value) VALUES ('visible')");

      try (ResultSet result =
          secondStatement.executeQuery(
              "SELECT value FROM test_support_values")) {
        assertTrue(result.next());
        assertEquals("visible", result.getString("value"));
      }

      try (ResultSet result =
          firstStatement.executeQuery("PRAGMA foreign_keys")) {
        assertTrue(result.next());
        assertEquals(1, result.getInt(1));
      }

      try (ResultSet result =
          secondStatement.executeQuery("PRAGMA foreign_keys")) {
        assertTrue(result.next());
        assertEquals(1, result.getInt(1));
      }

      assertThrows(
          SQLException.class,
          () ->
              firstStatement.executeUpdate(
                  "INSERT INTO USER_ROLES (user_id, role) "
                      + "VALUES (999, 'BIDDER')"));
      assertThrows(
          SQLException.class,
          () ->
              secondStatement.executeUpdate(
                  "INSERT INTO USER_ROLES (user_id, role) "
                      + "VALUES (998, 'SELLER')"));
    }
  }
}
