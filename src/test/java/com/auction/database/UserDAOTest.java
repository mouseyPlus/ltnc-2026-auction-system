package com.auction.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auction.model.Role;
import com.auction.model.User;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class UserDAOTest {

  @Test
  void createUserPersistsFieldsRolesAndGeneratedId(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);
    UserDAO dao = new UserDAO(support::openConnection);
    User user =
        new User(
            "alice",
            "alice@example.com",
            "0900000001",
            "already-hashed-password",
            "Alice",
            Instant.parse("2026-01-01T12:34:56Z"),
            true,
            Set.of(Role.BIDDER, Role.SELLER));

    Long id = dao.createUser(user);

    assertNotNull(id);
    assertTrue(id > 0);
    assertEquals(id, user.getId());

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet userResult =
            statement.executeQuery(
                "SELECT user_id, username, email, phone, password_hash, "
                    + "display_name, created_at_utc, is_active "
                    + "FROM USERS WHERE user_id = "
                    + id)) {
      assertTrue(userResult.next());
      assertEquals(id, userResult.getLong(1));
      assertEquals("alice", userResult.getString(2));
      assertEquals("alice@example.com", userResult.getString(3));
      assertEquals("0900000001", userResult.getString(4));
      assertEquals(
          "already-hashed-password",
          userResult.getString(5));
      assertEquals("Alice", userResult.getString(6));
      assertEquals(
          "2026-01-01T12:34:56Z",
          userResult.getString(7));
      assertEquals(1, userResult.getInt(8));
    }

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet roleResult =
            statement.executeQuery(
                "SELECT role FROM USER_ROLES WHERE user_id = "
                    + id
                    + " ORDER BY role")) {
      Set<String> roles = new LinkedHashSet<>();
      while (roleResult.next()) {
        roles.add(roleResult.getString("role"));
      }
      assertEquals(Set.of("BIDDER", "SELLER"), roles);
    }
  }

  @Test
  void findByIdReconstructsUserAndRoles(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);
    UserDAO dao = new UserDAO(support::openConnection);
    User source =
        new User(
            "bob",
            "bob@example.com",
            "0900000002",
            "hash-for-bob",
            "Bob",
            Instant.parse("2026-02-02T01:02:03Z"),
            false,
            Set.of(Role.BIDDER, Role.ADMIN));

    Long id = dao.createUser(source);
    Optional<User> found = dao.findById(id);

    assertTrue(found.isPresent());
    User restored = found.orElseThrow();
    assertEquals(id, restored.getId());
    assertEquals("bob", restored.getUsername());
    assertEquals("bob@example.com", restored.getEmail());
    assertEquals("0900000002", restored.getPhone());
    assertEquals("hash-for-bob", restored.getPasswordHash());
    assertEquals("Bob", restored.getDisplayName());
    assertEquals(
        Instant.parse("2026-02-02T01:02:03Z"),
        restored.getCreatedAtUtc());
    assertFalse(restored.isActive());
    assertEquals(Set.of(Role.BIDDER, Role.ADMIN), restored.getRoles());
  }

  @Test
  void findByIdReturnsEmptyForUnknownId(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);
    UserDAO dao = new UserDAO(support::openConnection);

    assertEquals(Optional.empty(), dao.findById(999999L));
  }

  @Test
  void createUserRejectsPreassignedId(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);
    UserDAO dao = new UserDAO(support::openConnection);
    User user =
        new User(
            7L,
            "preassigned",
            "preassigned@example.com",
            "0900000003",
            "hash",
            "Preassigned",
            Instant.parse("2026-03-03T00:00:00Z"),
            true,
            Set.of(Role.BIDDER));

    assertThrows(IllegalArgumentException.class, () -> dao.createUser(user));
  }

  @Test
  void createUserRollsBackRoleFailureAndLeavesIdNull(
      @TempDir Path temporaryDirectory) throws Exception {
    DatabaseTestSupport support = new DatabaseTestSupport(temporaryDirectory);
    UserDAO dao = new UserDAO(support::openConnection);
    User user =
        new User(
            "rollback",
            "rollback@example.com",
            "0900000004",
            "hash",
            "Rollback",
            Instant.parse("2026-04-04T00:00:00Z"),
            true,
            Set.of(Role.BIDDER, Role.ADMIN));

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(
          """
          CREATE TRIGGER fail_admin_role
          BEFORE INSERT ON USER_ROLES
          WHEN NEW.role = 'ADMIN'
          BEGIN
            SELECT RAISE(ABORT, 'forced ADMIN role insertion failure');
          END
          """);
    }

    assertThrows(SQLException.class, () -> dao.createUser(user));
    assertNull(user.getId());

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet userCount =
            statement.executeQuery("SELECT COUNT(*) FROM USERS")) {
      assertTrue(userCount.next());
      assertEquals(0, userCount.getInt(1));
    }

    try (Connection connection = support.openConnection();
        Statement statement = connection.createStatement();
        ResultSet roleCount =
            statement.executeQuery("SELECT COUNT(*) FROM USER_ROLES")) {
      assertTrue(roleCount.next());
      assertEquals(0, roleCount.getInt(1));
    }
  }
}
