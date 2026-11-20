package com.auction;
import com.auction.enums.AuctionStatus;
import com.auction.enums.Role;
import com.auction.model.Auction;
import com.auction.model.Bid;
import com.auction.model.Seller;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

// Test case
public class Main {
    public static void main(String[] args) {
        //1
        Set<Role> roles = new HashSet<>();
        roles.add(Role.SELLER);

        Seller seller = new Seller(
                1L,
                "seller01",
                "0123456789",
                "seller@gmail.com",
                "hash123",
                "Seller One", true,
                Instant.now(),
                roles
        );

        seller.addRole(Role.BIDDER);

        System.out.println(seller.getRoles());


        //2
        Auction auction = new Auction(
                1L,
                100L,
                10L,
                1_000_000L,
                Instant.now(),
                Instant.now().plusSeconds(86400),
                AuctionStatus.OPEN
        );

        Bid bid = new Bid(
                auction.getItemId(),1L,
                10L,
                2000000L,
                Instant.now());
    }
}
