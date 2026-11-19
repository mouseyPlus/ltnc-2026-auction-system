package com.auction.model;
public class Art extends Item {
    public Art(Long id, String name, Long ownerId, String description, String category) {
        super(id, name, ownerId,description, category);
    }
}