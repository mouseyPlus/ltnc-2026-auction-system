package com.auction.model;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.pattern.Observer;
import com.auction.enums.AuctionStatus;

import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
/*
 * AUCTIONS
 * -----------------------------
 * id              -> auction_id
 * item            -> item_id
 * sellerId        -> seller_id
 * startingPrice   -> starting_price_vnd
 * startTimeUtc    -> start_time_utc
 * endTimeUtc      -> end_time_utc
 * status          -> status
 */
public class Auction {
    private Long id;
    private Long itemId;
    private Long startingPrice;
    private Long sellerId;
    private Instant startTimeUtc;
    private Instant endTimeUtc;
    private List<Bid> bids;
    private AuctionStatus status;
    private ReentrantLock bidlock = new ReentrantLock();
    private List<Observer> observers;
    public Auction(Long id, Long itemId,Long sellerId, Long startingPrice, Instant startTimeUtc, Instant endTimeUtc, AuctionStatus status) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Must be higher than 0" );
        }
        this.id = id;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.bids = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.status = status;
        this.startTimeUtc = startTimeUtc;
        this.endTimeUtc = endTimeUtc;
    }
    public Long getCurrentPrice() {
        if (bids.isEmpty()) {
            return startingPrice;
        }
        return bids.get(bids.size() - 1).getAmount();
    }
    private long calculateMinimumIncrement() {
        long increment = (long) Math.ceil(getCurrentPrice() * 0.01);
        return Math.max(1L, increment);
    }
    private boolean isValidBid(Bid bid) {
        long currentPrice = getCurrentPrice();
        long minimumBid = currentPrice
                + calculateMinimumIncrement();
        return bid.getAmount() >= minimumBid;
    }
    public boolean placeBid(Bid bid) throws InvalidBidException, AuctionClosedException {
        bidlock.lock();
        try {
            if (status != AuctionStatus.OPEN
                    && status != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Auction is not running");
            }
            if (bid.getBidderId().equals(this.sellerId)) {
                throw new InvalidBidException("Seller cannot bid on own auction");
            }
            if (!isValidBid(bid)) {
                throw new InvalidBidException("Bid amount must be greater than current price");
            }
            bids.add(bid);
            notifyObservers(bid);
            return true;
        }
        finally {
            bidlock.unlock();
        }
    }
    public void startAuction() {
        if (status != AuctionStatus.OPEN) {
            throw new IllegalStateException("Auction should be OPEN first");
        }
        status = AuctionStatus.RUNNING;
    }
    public void finishAuction() {
        if (status != AuctionStatus.RUNNING) {
            throw new IllegalStateException("Auction isn't RUNNING");
        }
        status = AuctionStatus.FINISHED;
    }
    public void markAsPaid() {
        if (status != AuctionStatus.FINISHED) {
            throw new IllegalStateException("Only FINISHED auctions can be paid");
        }
        status = AuctionStatus.PAID;
    }
    public void cancelAuction() {
        if (status == AuctionStatus.PAID || status == AuctionStatus.FINISHED) {
            throw new IllegalStateException("Cannot cancel completed auction");
        }

        status = AuctionStatus.CANCELED;
    }
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    private void notifyObservers(Bid bid) {
        for (Observer observer : observers) {
            observer.update(bid);
        }
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Instant getStartTimeUtc() {
        return startTimeUtc;
    }

    public void setStartTimeUtc(Instant startTimeUtc) {
        this.startTimeUtc = startTimeUtc;
    }

    public Instant getEndTimeUtc() {
        return endTimeUtc;
    }

    public void setEndTimeUtc(Instant endTimeUtc) {
        this.endTimeUtc = endTimeUtc;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(Long startingPrice) {
        this.startingPrice = startingPrice;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public List<Bid> getBids() {
        return Collections.unmodifiableList(bids);
    }
}
