package com.auction.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void newUserKeepsRolesAndHasNoGeneratedId() {
    User user = createNewUser();

    assertNull(user.getId());
    assertEquals(Set.of(Role.BIDDER, Role.SELLER), user.getRoles());
  }

  @Test
  void assignGeneratedIdOnlySucceedsOnce() {
    User user = createNewUser();

    user.assignGeneratedId(42L);

    assertEquals(42L, user.getId());
    assertThrows(
        IllegalStateException.class,
        () -> user.assignGeneratedId(43L));
  }

  @Test
  void assignGeneratedIdRejectsInvalidValues() {
    User user = createNewUser();

    assertThrows(
        IllegalArgumentException.class,
        () -> user.assignGeneratedId(null));
    assertThrows(
        IllegalArgumentException.class,
        () -> user.assignGeneratedId(0L));
  }

  @Test
  void roleSetIsDefensiveAndCannotBeEmpty() {
    User user = createNewUser();
    Set<Role> roles = new LinkedHashSet<>();
    roles.add(Role.ADMIN);

    user.replaceRoles(roles);
    roles.clear();

    assertEquals(Set.of(Role.ADMIN), user.getRoles());
    assertThrows(
        UnsupportedOperationException.class,
        () -> user.getRoles().add(Role.BIDDER));
    assertThrows(
        IllegalArgumentException.class,
        () -> user.replaceRoles(Set.of()));
  }

  @Test
  void deactivateChangesOnlyActiveState() {
    User user = createNewUser();

    user.deactivate();

    assertFalse(user.isActive());
  }

  @Test
  void passwordHashIsStoredUnchanged() {
    User user = createNewUser();

    assertEquals("already-hashed-password", user.getPasswordHash());
  }

  @Test
  void persistedConstructorRequiresPositiveId() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                null,
                "user",
                "user@example.com",
                "0900000001",
                "already-hashed-password",
                "User",
                Instant.parse("2026-01-01T00:00:00Z"),
                true,
                Set.of(Role.BIDDER)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                0L,
                "user",
                "user@example.com",
                "0900000001",
                "already-hashed-password",
                "User",
                Instant.parse("2026-01-01T00:00:00Z"),
                true,
                Set.of(Role.BIDDER)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new User(
                -1L,
                "user",
                "user@example.com",
                "0900000001",
                "already-hashed-password",
                "User",
                Instant.parse("2026-01-01T00:00:00Z"),
                true,
                Set.of(Role.BIDDER)));
  }

  private User createNewUser() {
    return new User(
        "user",
        "user@example.com",
        "0900000001",
        "already-hashed-password",
        "User",
        Instant.parse("2026-01-01T00:00:00Z"),
        true,
        Set.of(Role.BIDDER, Role.SELLER));
  }
}
