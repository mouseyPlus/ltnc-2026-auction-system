package com.auction.model;
import com.auction.enums.Role;

import java.time.Instant;
import java.util.Set;

public class Seller extends User {
    public Seller(Long id,
                  String username, String phone,
                  String password, String email,String displayName, boolean isActive, Instant createdAtUtc, Set<Role> roles) {
        super(id, username,phone, password, email, displayName, isActive, createdAtUtc, roles);
        addRole(Role.SELLER);
    }
}
