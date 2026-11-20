package com.auction.model;

import java.time.Instant;

public class Vehicle extends Item {
    public Vehicle(Long id, String name, Long ownerId, String description, String category, Instant createdAtUtc, boolean isActive) {
        super(id, name, ownerId,description, category, createdAtUtc, isActive);
    }
}

