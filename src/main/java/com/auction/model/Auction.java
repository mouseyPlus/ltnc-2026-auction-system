package com.auction.model;
import com.auction.pattern.Observer;
import com.auction.enums.AuctionStatus;
import java.util.ArrayList;
import java.util.List;
/*
        * AUCTIONS
 * -----------------
         * id -> id
 * item -> item_id
 * sellerId -> seller_id
 * startingPrice -> starting_price
 * status -> status
 */
public class Auction {
    private Long id;
    private Item item;
    private Long startingPrice;
    private Long sellerId;
    private List<Bid> bids;
    private AuctionStatus status;
    private List<Observer> observers;
    public Auction(Long id, Item item,Long sellerId, Long startingPrice) {
        this.id = id;
        this.item = item;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.bids = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }
    public void placeBid(Bid bid) {
        bids.add(bid);
        notifyObservers(bid);
    }
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
    private void notifyObservers(Bid bid) {
        for (Observer observer : observers) {
            observer.Update(bid);
        }
    }
}
