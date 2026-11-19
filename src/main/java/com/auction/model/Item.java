package com.auction.model;
/*
 * ITEMS
 * -----------------------------
 * id              -> item_id
 * ownerId         -> owner_id
 * name            -> name
 * description     -> description
 * category        -> category
 * createdAtUtc    -> created_at_utc
 * active          -> is_active
 */

public abstract class Item {
    protected Long id;
    protected String name;
    protected Long ownerId;
    protected String description;
    protected String category;
    public Item(Long id, String name, Long ownerId, String description, String category) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.category = category;
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
