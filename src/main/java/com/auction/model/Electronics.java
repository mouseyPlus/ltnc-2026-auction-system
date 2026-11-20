package com.auction.model;

import java.time.Instant;

public class Electronics extends Item {
    public Electronics(Long id, String name, Long ownerId, String description, String category, Instant createdAtUtc, boolean isActive) {
        super(id, name, ownerId,description, category, createdAtUtc, isActive);
    }
}
