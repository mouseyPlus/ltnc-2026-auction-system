package com.auction.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Represents a registered auction-system user and its complete role set. */
public class User {

  private Long id;
  private String username;
  private String email;
  private String phone;
  private final String passwordHash;
  private String displayName;
  private final Instant createdAtUtc;
  private boolean isActive;
  private Set<Role> roles;

  /**
   * Creates a new user that has not yet received a generated database ID.
   *
   * @param username username for the account
   * @param email email address for the account
   * @param phone phone number for the account
   * @param passwordHash already-hashed password value
   * @param displayName display name for the account
   * @param createdAtUtc UTC creation timestamp
   * @param isActive whether the account is active
   * @param roles complete role set for the account
   */
  public User(
      String username,
      String email,
      String phone,
      String passwordHash,
      String displayName,
      Instant createdAtUtc,
      boolean isActive,
      Set<Role> roles) {
    this.passwordHash =
        Objects.requireNonNull(passwordHash, "passwordHash");
    this.createdAtUtc =
        Objects.requireNonNull(createdAtUtc, "createdAtUtc");
    initialize(
        null,
        username,
        email,
        phone,
        displayName,
        isActive,
        roles);
  }

  /**
   * Reconstructs a persisted user, including its generated ID and complete role set.
   *
   * @param id database-generated user ID
   * @param username username for the account
   * @param email email address for the account
   * @param phone phone number for the account
   * @param passwordHash already-hashed password value
   * @param displayName display name for the account
   * @param createdAtUtc UTC creation timestamp
   * @param isActive whether the account is active
   * @param roles complete role set for the account
   */
  public User(
      Long id,
      String username,
      String email,
      String phone,
      String passwordHash,
      String displayName,
      Instant createdAtUtc,
      boolean isActive,
      Set<Role> roles) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("User id must be positive");
    }
    this.passwordHash =
        Objects.requireNonNull(passwordHash, "passwordHash");
    this.createdAtUtc =
        Objects.requireNonNull(createdAtUtc, "createdAtUtc");
    initialize(
        id,
        username,
        email,
        phone,
        displayName,
        isActive,
        roles);
  }

  private void initialize(
      Long id,
      String username,
      String email,
      String phone,
      String displayName,
      boolean isActive,
      Set<Role> roles) {
    this.id = id;
    this.username = Objects.requireNonNull(username, "username");
    this.email = Objects.requireNonNull(email, "email");
    this.phone = Objects.requireNonNull(phone, "phone");
    this.displayName =
        Objects.requireNonNull(displayName, "displayName");
    this.isActive = isActive;
    this.roles = copyRoles(roles);
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Instant getCreatedAtUtc() {
    return createdAtUtc;
  }

  public boolean isActive() {
    return isActive;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setUsername(String username) {
    this.username = Objects.requireNonNull(username, "username");
  }

  public void setEmail(String email) {
    this.email = Objects.requireNonNull(email, "email");
  }

  public void setPhone(String phone) {
    this.phone = Objects.requireNonNull(phone, "phone");
  }

  public void setDisplayName(String displayName) {
    this.displayName =
        Objects.requireNonNull(displayName, "displayName");
  }

  /**
   * Assigns a database-generated ID after successful creation.
   *
   * @param id positive generated ID
   * @throws IllegalArgumentException if the ID is null or not positive
   * @throws IllegalStateException if the ID has already been assigned
   */
  public void assignGeneratedId(Long id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Generated user id must be positive");
    }
    if (this.id != null) {
      throw new IllegalStateException("User id has already been assigned");
    }
    this.id = id;
  }

  /**
   * Replaces the complete role set.
   *
   * @param roles non-null, non-empty role set
   */
  public void replaceRoles(Set<Role> roles) {
    this.roles = copyRoles(roles);
  }

  /** Marks this user inactive. */
  public void deactivate() {
    isActive = false;
  }

  private static Set<Role> copyRoles(Set<Role> roles) {
    if (roles == null || roles.isEmpty()) {
      throw new IllegalArgumentException("Roles must not be null or empty");
    }
    Set<Role> copy = new LinkedHashSet<>();
    for (Role role : roles) {
      copy.add(Objects.requireNonNull(role, "role"));
    }
    return Collections.unmodifiableSet(copy);
  }
}
