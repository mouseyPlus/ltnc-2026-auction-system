package com.auction.model;
import com.auction.enums.Role;
public class Admin extends User {
    public Admin(Long id, String username, String password, String email,String displayName, boolean isActive) {
        super(id, username, password, email, displayName, isActive);
        addRole(Role.ADMIN);
    }
}