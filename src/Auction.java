import java.util.List;
import java.util.ArrayList;
public class Auction {
    private int id;
    private Item item;
    private List<Bid> bids;
    private AuctionStatus status;
    public Auction(int id, Item item) {
        this.id = id;
        this.item = item;
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }
    public void placeBid(Bid bid) {
        bids.add(bid);
    }
}
