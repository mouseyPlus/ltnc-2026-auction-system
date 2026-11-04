import java.util.List;
import java.util.ArrayList;
public class AuctionManager {
    private static AuctionManager instance;
    private List<Auction> auctions;
    private AuctionManager() {
        auctions = new ArrayList<>();
    }
    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }
    public void addAuction(Auction auction) {
        auctions.add(auction);
    }
}