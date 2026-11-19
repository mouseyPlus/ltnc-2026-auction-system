package com.auction.model;
import com.auction.pattern.Observer;
import com.auction.enums.AuctionStatus;

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
    private Item item;
    private Long startingPrice;
    private Long sellerId;
    private Instant startTime;
    private Instant endTime;
    private List<Bid> bids;
    private AuctionStatus status;
    private ReentrantLock bidlock = new ReentrantLock();
    private List<Observer> observers;
    public Auction(Long id, Item item,Long sellerId, Long startingPrice, Instant startTime, Instant endTime) {
        if (startingPrice <= 0) {
            throw new IllegalArgumentException("Must be higher than 0" );
        }
        this.id = id;
        this.item = item;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.bids = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }
    public Long getCurrentPrice() {
        if (bids.isEmpty()) {
            return startingPrice;
        }
        return bids.get(bids.size() - 1).getAmount();
    }
    private boolean isValidBid(Bid bid) {
        return bid.getAmount() > getCurrentPrice();
    }
    public boolean placeBid(Bid bid) {
        bidlock.lock();
        try {
            if (status != AuctionStatus.OPEN
                    && status != AuctionStatus.RUNNING) {
                return false;
            }
            if (!bid.getBidderId().equals(this.id)) {
                return false;
            }
            if (!isValidBid(bid)) {
                return false;
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
}
