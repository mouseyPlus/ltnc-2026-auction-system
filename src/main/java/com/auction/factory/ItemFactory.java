package com.auction.factory;
import com.auction.model.Art;
import com.auction.model.Electronics;
import com.auction.model.Item;
import com.auction.model.Vehicle;
public class ItemFactory {
    public static Item createItem(Long id, String name, Long ownerId, String description, String category) {
        switch (category.toLowerCase()) {
            case "electronics":
                return new Electronics(id, name, ownerId,description, category);
            case "art":
                return new Art(id, name, ownerId,description, category);
            case "vehicle":
                return new Vehicle(id, name, ownerId,description, category);
            default:
                throw new IllegalArgumentException(
                        "Invalid item type");
        }
    }
}
