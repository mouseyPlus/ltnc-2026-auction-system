public class ItemFactory {
    public static Item createItem(String type, int id, String name, double startingPrice, double currentPrice, int sellerId) {
        switch (type.toLowerCase()) {
            case "electronics":
                return new Electronics(id, name, startingPrice, currentPrice, sellerId);
            case "art":
                return new Art(id, name, startingPrice, currentPrice, sellerId);
            case "vehicle":
                return new Vehicle(id, name, startingPrice, currentPrice, sellerId);
            default:
                throw new IllegalArgumentException("Invalid item type");
        }
    }
}
