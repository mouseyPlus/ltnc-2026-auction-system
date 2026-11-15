public abstract class Item {
    protected int id;
    protected String name;
    protected double startingPrice;
    protected double currentPrice;
    protected int sellerId;
    public Item(int id, String name, double startingPrice, double currentPrice, int sellerId) {
        this.id = id;
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.sellerId = sellerId;
    }
}
