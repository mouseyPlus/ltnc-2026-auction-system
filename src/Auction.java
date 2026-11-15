import java.util.ArrayList;
import java.util.List;
public class Auction {
    private Long id;
    private Item item;
    private Integer startingPrice;
    private List<Bid> bids;
    private AuctionStatus status;
    public Auction(Long id, Item item, Integer startingPrice) {
        this.id = id;
        this.item = item;
        this.startingPrice = startingPrice;
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }
    public void placeBid(Bid bid) {
        bids.add(bid);
    }
}
