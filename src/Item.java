public abstract class Item {
    protected Long id;
    protected String name;
    protected Long sellerId;
    public Item(Long id, String name, Long sellerId) {
        this.id = id;
        this.name = name;
        this.sellerId = sellerId;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Long getSellerId() {
        return sellerId;
    }
}
