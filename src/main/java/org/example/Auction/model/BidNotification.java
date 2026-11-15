package org.example.Auction.model;

import org.example.Auction.pattern.Observer;

public class BidNotification implements Observer {
    private String observerName;
    public BidNotification(String observerName) {
        this.observerName = observerName;
    }
    @Override
    public void Update(Bid bid) {
        System.out.println(observerName + " received notification: New bid " + bid.getAmount());
    }
}
