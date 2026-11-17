package com.auction.model;
import com.auction.enums.Role;
public class Seller extends User {
    public Seller(Long id, String username, String password) {
        super(id, username, password);
        addRole(Role.SELLER);
    }
}
