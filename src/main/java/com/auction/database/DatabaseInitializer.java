package com.auction.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Loads the database schema and initializes the auction database. */
public final class DatabaseInitializer {

  private static final String SCHEMA_RESOURCE = "/db/schema.sql";

  private DatabaseInitializer() {}

  /**
   * Initializes the database using a newly opened shared connection.
   *
   * @throws SQLException if the connection or schema initialization fails
   */
  public static void initialize() throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {
      initialize(connection);
    }
  }

  static void initialize(Connection connection) throws SQLException {
    String schemaSql = loadSchema();

    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(schemaSql);
    } catch (SQLException exception) {
      throw new SQLException("Failed to initialize the database schema", exception);
    }
  }

  private static String loadSchema() throws SQLException {
    try (InputStream input =
        DatabaseInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
      if (input == null) {
        throw new SQLException("Schema resource not found: " + SCHEMA_RESOURCE);
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new SQLException("Failed to read schema resource: " + SCHEMA_RESOURCE, exception);
    }
  }
}
