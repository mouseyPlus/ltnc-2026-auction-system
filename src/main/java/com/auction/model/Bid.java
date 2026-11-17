package com.auction.model;
import java.time.Instant;
/*
 * BIDS
 * -----------------
 * auctionId -> auction_id
 * bidderId -> bidder_id
 * amount -> amount
 * timestamp -> created_at
 */
public class Bid {
    private Long bidderId;
    private Long amount;
    private Instant timestamp;
    private Long auctionId;
    public Bid(Long bidderId, Long amount,Long auctionId) {
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = Instant.now();
        this.auctionId = auctionId;
    }
    public Long getBidderId() {
        return bidderId;
    }
    public Long getAmount() {
        return amount;
    }
}