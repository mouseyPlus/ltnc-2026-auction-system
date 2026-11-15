package org.example.Auction.model;
import org.example.Auction.pattern.Observer;
import org.example.Auction.enums.AuctionStatus;
import java.util.ArrayList;
import java.util.List;
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
    }
    public void addObserver(Observer observer) {
        observers.add(observer);
    }
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }
}
