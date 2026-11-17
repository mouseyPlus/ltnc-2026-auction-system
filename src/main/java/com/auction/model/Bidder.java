package com.auction.model;
import com.auction.enums.Role;
public class Bidder extends User {
    public Bidder(Long id, String username, String password) {
        super(id, username, password);
        addRole(Role.BIDDER);
    }
}
