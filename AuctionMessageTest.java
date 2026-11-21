import java.io.Serializable;

public class AuctionMessageTest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        JOIN,
        BID,
        QUIT,
        START,
        BID_SUCCESSFUL,
        BID_REJECTED,
        ERROR
    }

    private final Type type;
    private final String sender;
    private final Long bidAmount;
    private final long currentPrice;
    private final String highestBidder;
    private final String message;

    private AuctionMessageTest(
            Type type,
            String sender,
            Long bidAmount,
            long currentPrice,
            String highestBidder,
            String message
    ) {
        this.type = type;
        this.sender = sender;
        this.bidAmount = bidAmount;
        this.currentPrice = currentPrice;
        this.highestBidder = highestBidder;
        this.message = message;
    }

    public static AuctionMessageTest join(String sender) {
        return new AuctionMessageTest(Type.JOIN, sender, null, 0, null, null);
    }

    public static AuctionMessageTest bid(String sender, long bidAmount) {
        return new AuctionMessageTest(Type.BID, sender, bidAmount, 0, null, null);
    }

    public static AuctionMessageTest quit(String sender) {
        return new AuctionMessageTest(Type.QUIT, sender, null, 0, null, "Auction terminated.");
    }

    public static AuctionMessageTest start(long currentPrice, String highestBidder) {
        return new AuctionMessageTest(Type.START, null, null, currentPrice, highestBidder,
                "Initial price: " + currentPrice + ". Current highest bidder: " + highestBidder);
    }

    public static AuctionMessageTest bidSuccessful(long currentPrice, String highestBidder) {
        return new AuctionMessageTest(Type.BID_SUCCESSFUL, highestBidder, currentPrice, currentPrice, highestBidder,
                "New highest price is " + currentPrice + " by " + highestBidder + "!");
    }

    public static AuctionMessageTest bidRejected(long currentPrice, String highestBidder, String reason) {
        return new AuctionMessageTest(Type.BID_REJECTED, highestBidder, null, currentPrice, highestBidder, reason);
    }

    public static AuctionMessageTest error(String reason) {
        return new AuctionMessageTest(Type.ERROR, null, null, 0, null, reason);
    }

    public Type getType() { return type; }
    public String getSender() { return sender; }
    public Long getBidAmount() { return bidAmount; }
    public Long getCurrentPrice() { return currentPrice; }
    public String getHighestBidder() { return highestBidder; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return switch(type) {
            case START -> "[START]: " + message;
            case BID_SUCCESSFUL -> "[BID_SUCCESSFUL]: " + message;
            case BID_REJECTED -> "[BID_REJECTED]: " + message;
            case ERROR -> "[ERROR]: " + message;
            case QUIT -> "[QUIT]: " + message;
            default -> "[" + type + "] " + sender + (bidAmount != null ? " bid " + bidAmount + "" : " joined.");
        };
    }
}