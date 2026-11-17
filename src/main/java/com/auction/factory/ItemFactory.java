package com.auction.factory;
import com.auction.model.Art;
import com.auction.model.Electronics;
import com.auction.model.Item;
import com.auction.model.Vehicle;
public class ItemFactory {
    public static Item createItem(String type, Long id, String name, Long ownerId) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(id, name, ownerId);
            case "art":
                return new Art(id, name, ownerId);
            case "vehicle":
                return new Vehicle(id, name, ownerId);
            default:
                throw new IllegalArgumentException(
                        "Invalid item type");
        }
    }
}
