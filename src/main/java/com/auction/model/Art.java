package com.auction.model;

import java.time.Instant;

public class Art extends Item {
    public Art(Long id, String name, Long ownerId, String description, String category, Instant createdAtUtc, boolean isActive) {
        super(id, name, ownerId,description, category, createdAtUtc, isActive);
    }
}