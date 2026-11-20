package com.auction.model;
import java.time.Instant;
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
    protected Long itemId;
    protected String name;
    protected Long ownerId;
    protected String description;
    protected String category;
    protected Instant createdAtUtc;
    protected boolean isActive;
    public Item(Long id, String name, Long ownerId, String description, String category,Instant createdAtUtc, boolean isActive) {
        this.itemId = id;
        this.name = name;
        this.ownerId = ownerId;
        this.description = description;
        this.category = category;
        this.createdAtUtc = createdAtUtc;
        this.isActive = isActive;
    }
    public Long getId() {
        return itemId;
    }
    public String getName() {
        return name;
    }
    public Long getOwnerId() {
        return ownerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
