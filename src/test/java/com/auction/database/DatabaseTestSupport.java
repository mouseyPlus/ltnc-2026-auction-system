  package com.auction.database;

  import java.io.IOException;
  import java.nio.file.Files;
  import java.nio.file.Path;
  import java.sql.Connection;
  import java.sql.DriverManager;
  import java.sql.SQLException;
  import java.sql.Statement;
  import java.util.Objects;

  /** Provides isolated file-backed SQLite databases for DAO tests. */
  final class DatabaseTestSupport {

    private final String jdbcUrl;

    /**
     * Creates and initializes a temporary SQLite database.
     *
     * @param temporaryDirectory directory supplied by JUnit's {@code @TempDir}
     * @throws IOException if the temporary directory cannot be created
     * @throws SQLException if the database cannot be initialized
     */
    DatabaseTestSupport(Path temporaryDirectory) throws IOException, SQLException {
      Objects.requireNonNull(temporaryDirectory, "temporaryDirectory");
      Files.createDirectories(temporaryDirectory);

      Path databaseFile =
          temporaryDirectory.resolve("auction-test.db").toAbsolutePath().normalize();
      jdbcUrl = "jdbc:sqlite:" + databaseFile.toString().replace('\\', '/');

      try (Connection connection = openConnection()) {
        DatabaseInitializer.initialize(connection);
      }
    }

    /**
     * Opens a new connection to the temporary database.
     *
     * @return a connection with foreign-key enforcement enabled
     * @throws SQLException if the connection cannot be initialized
     */
    Connection openConnection() throws SQLException {
      Connection connection = null;
      try {
        connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
          statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
      } catch (SQLException initializationFailure) {
        if (connection == null) {
          throw initializationFailure;
        }

        try {
          connection.close();
        } catch (SQLException closeFailure) {
          initializationFailure.addSuppressed(closeFailure);
        }
        throw initializationFailure;
      }
    }
  }
