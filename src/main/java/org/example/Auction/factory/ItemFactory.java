package org.example.Auction.factory;
import org.example.Auction.model.Art;
import org.example.Auction.model.Electronics;
import org.example.Auction.model.Item;
import org.example.Auction.model.Vehicle;
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
