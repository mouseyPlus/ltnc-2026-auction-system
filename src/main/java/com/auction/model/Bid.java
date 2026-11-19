package com.auction.model;
import java.time.Instant;
/*
 * BIDS
 * -----------------------------
 * id              -> bid_id
 * auctionId       -> auction_id
 * bidderId        -> bidder_id
 * amount          -> amount_vnd
 * timestamp       -> bid_time_utc
 */
public class Bid {
    private Long bidId;
    private Long bidderId;
    private Long amount;
    private Instant timestamp;
    private Long auctionId;
    public Bid(Long bidId, Long bidderId, Long amount,Long auctionId) {
        this.bidId = bidId;
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