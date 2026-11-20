package com.auction.database;

import com.auction.model.Role;
import com.auction.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Provides SQLite persistence operations for users. */
// CHECKSTYLE.OFF: AbbreviationAsWordInName
public class UserDAO {

  private final ConnectionProvider connectionProvider;

  /**
   * Creates a user DAO with a connection provider.
   *
   * @param connectionProvider source of owned SQLite connections
   */
  public UserDAO(ConnectionProvider connectionProvider) {
    this.connectionProvider =
        Objects.requireNonNull(connectionProvider, "connectionProvider");
  }
  // CHECKSTYLE.ON: AbbreviationAsWordInName

  /**
   * Creates a user and all of its roles in one transaction.
   *
   * @param user new user with a null ID
   * @return generated positive user ID
   * @throws SQLException if insertion or transaction management fails
   * @throws IllegalArgumentException if user or creation arguments are invalid
   */
  public Long createUser(User user) throws SQLException {
    if (user == null) {
      throw new IllegalArgumentException("User must not be null");
    }
    if (user.getId() != null) {
      throw new IllegalArgumentException(
          "New users must not have a preassigned id");
    }
    Set<Role> roles = user.getRoles();
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("Roles must not be null or empty");
    }

    try (Connection connection = connectionProvider.getConnection()) {
      try {
        connection.setAutoCommit(false);

        long generatedId;
        try (PreparedStatement statement =
            connection.prepareStatement(
                """
                INSERT INTO USERS (
                    username, email, phone, password_hash,
                    display_name, created_at_utc, is_active
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS)) {
          statement.setString(1, user.getUsername());
          statement.setString(2, user.getEmail());
          statement.setString(3, user.getPhone());
          statement.setString(4, user.getPasswordHash());
          statement.setString(5, user.getDisplayName());
          statement.setString(6, user.getCreatedAtUtc().toString());
          statement.setInt(7, user.isActive() ? 1 : 0);
          statement.executeUpdate();

          generatedId = readGeneratedId(connection, statement);
        }

        try (PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO USER_ROLES (user_id, role) VALUES (?, ?)")) {
          for (Role role : roles) {
            statement.setLong(1, generatedId);
            statement.setString(2, role.name());
            statement.executeUpdate();
          }
        }

        connection.commit();
        user.assignGeneratedId(generatedId);
        return generatedId;
      } catch (SQLException | RuntimeException failure) {
        rollback(connection, failure);
        throw failure;
      }
    }
  }

  /**
   * Retrieves a complete user by generated ID.
   *
   * @param id positive persisted user ID
   * @return user and complete role set, or empty if not found
   * @throws SQLException if lookup fails
   * @throws IllegalArgumentException if id is null or not positive
   */
  public Optional<User> findById(Long id) throws SQLException {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("User id must be positive");
    }

    try (Connection connection = connectionProvider.getConnection()) {
      User user;
      try (PreparedStatement statement =
          connection.prepareStatement(
              """
              SELECT user_id, username, email, phone, password_hash,
                     display_name, created_at_utc, is_active
              FROM USERS
              WHERE user_id = ?
              """)) {
        statement.setLong(1, id);
        try (ResultSet resultSet = statement.executeQuery()) {
          if (!resultSet.next()) {
            return Optional.empty();
          }
          user =
              new User(
                  resultSet.getLong("user_id"),
                  resultSet.getString("username"),
                  resultSet.getString("email"),
                  resultSet.getString("phone"),
                  resultSet.getString("password_hash"),
                  resultSet.getString("display_name"),
                  Instant.parse(resultSet.getString("created_at_utc")),
                  resultSet.getInt("is_active") == 1,
                  readRoles(connection, id));
        }
      }
      return Optional.of(user);
    }
  }

  private long readGeneratedId(
      Connection connection,
      PreparedStatement insertStatement) throws SQLException {
    try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
      if (generatedKeys.next()) {
        long id = generatedKeys.getLong(1);
        if (id > 0) {
          return id;
        }
      }
    }

    try (Statement statement = connection.createStatement();
        ResultSet resultSet =
            statement.executeQuery("SELECT last_insert_rowid()")) {
      if (resultSet.next()) {
        long id = resultSet.getLong(1);
        if (id > 0) {
          return id;
        }
      }
    }
    throw new SQLException("SQLite did not return a generated user id");
  }

  private Set<Role> readRoles(Connection connection, long userId)
      throws SQLException {
    Set<Role> roles = new LinkedHashSet<>();
    try (PreparedStatement statement =
        connection.prepareStatement(
            "SELECT role FROM USER_ROLES WHERE user_id = ?")) {
      statement.setLong(1, userId);
      try (ResultSet resultSet = statement.executeQuery()) {
        while (resultSet.next()) {
          roles.add(Role.valueOf(resultSet.getString("role")));
        }
      }
    }
    return roles;
  }

  private void rollback(
      Connection connection,
      Throwable failure) {
    try {
      connection.rollback();
    } catch (SQLException rollbackFailure) {
      failure.addSuppressed(rollbackFailure);
    }
  }
}
