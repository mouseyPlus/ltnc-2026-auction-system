package org.example.Auction.model;
public abstract class Item {
    protected Long id;
    protected String name;
    protected Long ownerId;
    public Item(Long id, String name, Long ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Long getOwnerId() {
        return ownerId;
    }
}
