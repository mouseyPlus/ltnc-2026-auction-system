package com.auction.model;

import com.auction.enums.Role;

import java.util.HashSet;
import java.util.Set;
/*
 * Entity Mapping
 *
 * USERS
 * -----------------
 * id -> id
 * username -> username
 * password -> password
 *
 * USER_ROLES
 * -----------------
 * user_id -> USERS.id
 * role -> BIDDER | SELLER | ADMIN
 */
public abstract class User {
    protected Long id;
    protected String username;
    protected String password;
    protected Set<Role> roles;
    public User(Long id,
                String username,
                String password) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>();
    }
    public void addRole(Role role) {
        roles.add(role);
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
}
