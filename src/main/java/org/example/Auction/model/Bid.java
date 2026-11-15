package org.example.Auction.model;
import java.time.Instant;
public class Bid {
    private Long bidderId;
    private Long amount;
    private Instant timestamp;
    public Bid(Long bidderId, Long amount) {
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = Instant.now();
    }
    public Long getBidderId() {
        return bidderId;
    }
    public Long getAmount() {
        return amount;
    }
}