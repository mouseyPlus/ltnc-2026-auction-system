package com.auction.model;
import com.auction.enums.Role;
public class Admin extends User {
    public Admin(Long id, String username, String password) {
        super(id, username, password);
        addRole(Role.ADMIN);
    }
}