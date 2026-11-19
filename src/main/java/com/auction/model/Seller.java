package com.auction.model;
import com.auction.enums.Role;
public class Seller extends User {
    public Seller(Long id,
                  String username,
                  String password, String email,String displayName, boolean isActive) {
        super(id, username, password, email, displayName, isActive);
        addRole(Role.SELLER);
    }
}
