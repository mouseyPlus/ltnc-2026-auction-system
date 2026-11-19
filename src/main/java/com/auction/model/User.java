package com.auction.model;

import com.auction.enums.Role;
import java.util.Collections;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
/*
 * USERS
 * -----------------------------
 * id              -> user_id
 * username        -> username
 * email           -> email
 * passwordHash    -> password_hash
 * displayName     -> display_name
 * createdAtUtc    -> created_at_utc
 * active          -> is_active
 *
 * USER_ROLES
 * -----------------------------
 * user_id
 * role
 */
public abstract class User {
    protected Long id;
    protected String username;
    protected String email;
    protected String passwordHash;
    protected String displayName;
    protected Instant createdAtUtc;
    protected Set<Role> roles;
    protected boolean isActive;
    public User(Long id,
                String username,
                String password, String email,String displayName, boolean isActive) {
        this.id = id;
        this.username = username;
        this.passwordHash = password;
        this.roles = new HashSet<>();
        this.email = email;
        this.displayName = displayName;
        this.isActive = false;
        this.createdAtUtc = Instant.now();
    }
    public void addRole(Role role) {
        roles.add(role);
    }
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
}
