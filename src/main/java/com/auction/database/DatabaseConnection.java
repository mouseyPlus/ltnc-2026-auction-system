package com.auction.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Provides shared access to the application's SQLite database connection. */
public final class DatabaseConnection {

  private static final String DATABASE_URL = "jdbc:sqlite:data/auction.db";
  private static final Path DATABASE_DIRECTORY = Path.of("data");

  private DatabaseConnection() {}

  /**
   * Opens a SQLite connection and enables foreign-key enforcement.
   *
   * @return a connection configured for the auction database
   * @throws SQLException if the database directory or connection cannot be initialized
   */
  public static Connection getConnection() throws SQLException {
    createDatabaseDirectory();

    Connection connection = null;
    try {
      connection = DriverManager.getConnection(DATABASE_URL);
      try (Statement statement = connection.createStatement()) {
        statement.execute("PRAGMA foreign_keys = ON");
      }
      return connection;
    } catch (SQLException exception) {
      closeAfterInitializationFailure(connection, exception);
      throw exception;
    }
  }

  private static void createDatabaseDirectory() throws SQLException {
    try {
      Files.createDirectories(DATABASE_DIRECTORY);
    } catch (IOException exception) {
      throw new SQLException("Unable to create the database directory", exception);
    }
  }

  private static void closeAfterInitializationFailure(
      Connection connection,
      SQLException initializationFailure) {
    if (connection == null) {
      return;
    }

    try {
      connection.close();
    } catch (SQLException closeFailure) {
      initializationFailure.addSuppressed(closeFailure);
    }
  }
}
