package com.auction.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseInitializerVersionTest {

  @Test
  void versionOneDatabaseWithAllApplicationTablesMissingIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("missing-tables.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA user_version = 1");

      assertThrows(
          SQLException.class,
          () -> DatabaseInitializer.initialize(connection));

      try (ResultSet tables =
          statement.executeQuery(
              "SELECT COUNT(*) FROM sqlite_master "
                  + "WHERE type = 'table' AND ("
                  + "name = 'USERS' OR name = 'USER_ROLES' "
                  + "OR name = 'ITEMS' OR name = 'AUCTIONS' "
                  + "OR name = 'BIDS')")) {
        assertTrue(tables.next());
        assertEquals(0, tables.getInt(1));
      }
      try (ResultSet version =
          statement.executeQuery("PRAGMA user_version")) {
        assertTrue(version.next());
        assertEquals(1, version.getInt(1));
      }
    }
  }

  @Test
  void genuinelyEmptyVersionZeroDatabaseInitializesSuccessfully(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("fresh.db").toAbsolutePath());
        Statement statement = connection.createStatement();
        ResultSet version =
            statement.executeQuery("PRAGMA user_version")) {
      assertTrue(version.next());
      assertEquals(0, version.getInt(1));
    }

    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("fresh.db").toAbsolutePath())) {
      try (Statement statement = connection.createStatement();
          ResultSet applicationTables =
              statement.executeQuery(
                  "SELECT COUNT(*) FROM sqlite_master "
                      + "WHERE type = 'table' AND name IN "
                      + "(\"USERS\", \"USER_ROLES\", \"ITEMS\", "
                      + "\"AUCTIONS\", \"BIDS\")")) {
        assertTrue(applicationTables.next());
        assertEquals(0, applicationTables.getInt(1));
      }

      DatabaseInitializer.initialize(connection);
    }

    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("fresh.db").toAbsolutePath());
        Statement statement = connection.createStatement();
        ResultSet version =
            statement.executeQuery("PRAGMA user_version")) {
      assertTrue(version.next());
      assertEquals(1, version.getInt(1));
    }
  }

  @Test
  void freshDatabaseInitializesAtVersionOne(@TempDir Path temporaryDirectory)
      throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery("PRAGMA user_version")) {
      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getInt(1));
    }
  }

  @Test
  void freshInitializationCanBeRepeated(@TempDir Path temporaryDirectory)
      throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection()) {
      DatabaseInitializer.initialize(connection);
      DatabaseInitializer.initialize(connection);
    }

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery("PRAGMA user_version")) {
      assertTrue(resultSet.next());
      assertEquals(1, resultSet.getInt(1));
    }
  }

  @Test
  void compatibleVersionZeroDatabaseIsAdoptedWithoutLosingData(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA user_version = 0");
      statement.executeUpdate(
          "INSERT INTO USERS "
              + "(username, email, phone, password_hash, display_name, "
              + "created_at_utc, is_active) "
              + "VALUES ('preserved', 'preserved@example.com', '0900000001', "
              + "'hash', 'Preserved User', '2026-01-01T00:00:00Z', 1)");

      DatabaseInitializer.initialize(connection);

      try (ResultSet resultSet =
          statement.executeQuery(
              "SELECT username, email, phone FROM USERS "
                  + "WHERE username = 'preserved'")) {
        assertTrue(resultSet.next());
        assertEquals("preserved", resultSet.getString(1));
        assertEquals("preserved@example.com", resultSet.getString(2));
        assertEquals("0900000001", resultSet.getString(3));
      }

      try (ResultSet versionResult =
          statement.executeQuery("PRAGMA user_version")) {
        assertTrue(versionResult.next());
        assertEquals(1, versionResult.getInt(1));
      }
    }
  }

  @Test
  void legacyDatabaseWithoutPhoneIsRejected(@TempDir Path temporaryDirectory)
      throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("legacy.db").toAbsolutePath())) {
      try (Statement statement = connection.createStatement()) {
        statement.execute(
            "CREATE TABLE USERS ("
                + "user_id INTEGER PRIMARY KEY, "
                + "username TEXT NOT NULL UNIQUE, "
                + "email TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL, "
                + "display_name TEXT NOT NULL, "
                + "created_at_utc TEXT NOT NULL, "
                + "is_active INTEGER NOT NULL DEFAULT 1 "
                + "CHECK (is_active IN (0, 1)))");
        statement.executeUpdate(
            "INSERT INTO USERS "
                + "(username, email, password_hash, display_name, "
                + "created_at_utc, is_active) "
                + "VALUES ('legacy', 'legacy@example.com', 'hash', "
                + "'Legacy User', '2026-01-01T00:00:00Z', 1)");
      }

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("phone"));

      try (Statement statement = connection.createStatement();
          ResultSet columns =
              statement.executeQuery("PRAGMA table_info(USERS)")) {
        boolean phoneFound = false;
        while (columns.next()) {
          phoneFound |= "phone".equals(columns.getString(2));
        }
        assertTrue(!phoneFound);
      }

      try (Statement statement = connection.createStatement();
          ResultSet users =
              statement.executeQuery("SELECT username FROM USERS")) {
        assertTrue(users.next());
        assertEquals("legacy", users.getString(1));
      }

      try (Statement statement = connection.createStatement();
          ResultSet version =
              statement.executeQuery("PRAGMA user_version")) {
        assertTrue(version.next());
        assertEquals(0, version.getInt(1));
      }
    }
  }

  @Test
  void versionOneDatabaseWithMissingTableIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE BIDS");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("Incompatible database schema"));
    }
  }

  @Test
  void unsupportedHigherVersionIsRejected(@TempDir Path temporaryDirectory)
      throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA user_version = 2");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("Unsupported schema version"));
      try (ResultSet resultSet =
          statement.executeQuery("PRAGMA user_version")) {
        assertTrue(resultSet.next());
        assertEquals(2, resultSet.getInt(1));
      }
    }
  }

  @Test
  void versionOneDatabaseWithMissingUsersPrimaryKeyIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("missing-users-pk.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      String schema = loadSchema().replace(
          "user_id INTEGER PRIMARY KEY,", "user_id INTEGER,");
      statement.executeUpdate(schema);
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("primary key"));
      try (ResultSet version =
          statement.executeQuery("PRAGMA user_version")) {
        assertTrue(version.next());
        assertEquals(1, version.getInt(1));
      }
    }
  }

  @Test
  void versionOneDatabaseWithMissingUserRolesForeignKeyIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("missing-role-fk.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      statement.executeUpdate(loadSchema());
      statement.execute("DROP TABLE USER_ROLES");
      statement.execute(
          "CREATE TABLE USER_ROLES ("
              + "user_id INTEGER NOT NULL, "
              + "role TEXT NOT NULL "
              + "CHECK (role IN ('BIDDER', 'SELLER', 'ADMIN')), "
              + "PRIMARY KEY (user_id, role))");
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("foreign key"));
      try (ResultSet version =
          statement.executeQuery("PRAGMA user_version")) {
        assertTrue(version.next());
        assertEquals(1, version.getInt(1));
      }
    }
  }

  @Test
  void userRolesCompositeKeyWithoutNotNullIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("missing-role-not-null.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      statement.executeUpdate(loadSchema());
      statement.execute("DROP TABLE USER_ROLES");
      statement.execute(
          "CREATE TABLE USER_ROLES ("
              + "user_id INTEGER, "
              + "role TEXT, "
              + "PRIMARY KEY (user_id, role), "
              + "FOREIGN KEY (user_id) REFERENCES USERS (user_id) "
              + "ON DELETE RESTRICT)");
      statement.execute(
          "PRAGMA foreign_key_check");
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("NOT NULL"));
    }
  }

  @Test
  void partialUniqueIndexCannotReplaceFullPhoneUniqueConstraint(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("partial-phone.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      String schema = loadSchema()
          .replace("username TEXT NOT NULL UNIQUE,", "username TEXT NOT NULL,")
          .replace("email TEXT NOT NULL UNIQUE,", "email TEXT NOT NULL,")
          .replace("phone TEXT NOT NULL UNIQUE,", "phone TEXT NOT NULL,");
      statement.executeUpdate(schema);
      statement.execute(
          "CREATE UNIQUE INDEX ux_phone_partial "
              + "ON USERS (phone) WHERE phone <> ''");
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("UNIQUE"));
    }
  }

  @Test
  void activeAuctionIndexWithExtendedPredicateIsRejected(
      @TempDir Path temporaryDirectory) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve("extended-active-index.db").toAbsolutePath());
        Statement statement = connection.createStatement()) {
      statement.executeUpdate(loadSchema());
      statement.execute("DROP INDEX ux_auctions_active_item");
      statement.execute(
          "CREATE UNIQUE INDEX ux_auctions_active_item "
              + "ON AUCTIONS (item_id) "
              + "WHERE status IN ('OPEN', 'RUNNING') AND 1 = 0");
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains("incompatible predicate"));
    }
  }

  @Test
  void itemsCategoryCheckIsRequired(@TempDir Path temporaryDirectory)
      throws Exception {
    String original =
        "category TEXT NOT NULL CHECK "
            + "(category IN ('ELECTRONICS', 'ART', 'VEHICLE'))";
    assertSchemaCheckIsRequired(
        temporaryDirectory,
        "missing-items-category-check.db",
        original,
        "category TEXT NOT NULL",
        "ITEMS.category");
  }

  @Test
  void auctionsStatusCheckIsRequired(@TempDir Path temporaryDirectory)
      throws Exception {
    String original =
        "status TEXT NOT NULL CHECK (\n"
            + "        status IN ('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED')\n"
            + "    )";
    assertSchemaCheckIsRequired(
        temporaryDirectory,
        "missing-auctions-status-check.db",
        original,
        "status TEXT NOT NULL",
        "AUCTIONS.status");
  }

  @Test
  void auctionsEndTimeCheckIsRequired(@TempDir Path temporaryDirectory)
      throws Exception {
    String original = "    CHECK (end_time_utc > start_time_utc),";
    assertSchemaCheckIsRequired(
        temporaryDirectory,
        "missing-auctions-end-time-check.db",
        original,
        "",
        "AUCTIONS.end_time_utc");
  }

  private void assertSchemaCheckIsRequired(
      Path temporaryDirectory,
      String databaseName,
      String originalFragment,
      String replacement,
      String expectedMessage) throws Exception {
    try (Connection connection =
        java.sql.DriverManager.getConnection(
            "jdbc:sqlite:"
                + temporaryDirectory.resolve(databaseName).toAbsolutePath());
        Statement statement = connection.createStatement()) {
      String schema =
          loadSchema().replace("\r\n", "\n");
      assertTrue(
          schema.contains(originalFragment),
          "Test schema did not contain expected fragment");
      String malformedSchema =
          schema.replace(originalFragment, replacement);
      assertFalse(
          malformedSchema.equals(schema),
          "Malformed schema replacement did not change schema");
      assertFalse(
          malformedSchema.contains(originalFragment),
          "Malformed schema still contains replaced constraint");

      statement.executeUpdate(malformedSchema);
      statement.execute("PRAGMA user_version = 1");

      SQLException failure =
          assertThrows(
              SQLException.class,
              () -> DatabaseInitializer.initialize(connection));
      assertTrue(failure.getMessage().contains(expectedMessage));
    }
  }

  @Test
  void foreignKeysRemainEnabledThroughInitialization(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement()) {
      DatabaseInitializer.initialize(connection);

      try (ResultSet resultSet =
          statement.executeQuery("PRAGMA foreign_keys")) {
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt(1));
      }

      assertThrows(
          SQLException.class,
          () ->
              statement.executeUpdate(
                  "INSERT INTO USER_ROLES (user_id, role) "
                      + "VALUES (999, 'BIDDER')"));
    }
  }

  private String loadSchema() throws IOException {
    try (InputStream input =
        getClass().getResourceAsStream("/db/schema.sql")) {
      if (input == null) {
        throw new IOException("Schema resource not found");
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    }
  }
}
