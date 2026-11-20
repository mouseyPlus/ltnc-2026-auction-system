package com.auction.factory;
import com.auction.model.Art;
import com.auction.model.Electronics;
import com.auction.model.Item;
import com.auction.model.Vehicle;

import java.time.Instant;

public class ItemFactory {
    public static Item createItem(Long id, String name, Long ownerId, String description, String category, Instant createdAtUtc, boolean isActive) {
        switch (category.toLowerCase()) {
            case "electronics":
                return new Electronics(id, name, ownerId,description, category, createdAtUtc, isActive);
            case "art":
                return new Art(id, name, ownerId,description, category, createdAtUtc, isActive);
            case "vehicle":
                return new Vehicle(id, name, ownerId,description, category, createdAtUtc, isActive);
            default:
                throw new IllegalArgumentException(
                        "Invalid item type");
        }
    }
}
