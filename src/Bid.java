import java.time.LocalDateTime;
public class Bid {
    private int bidderId;
    private double amount;
    private LocalDateTime timestamp;
    public Bid(int bidderId, double amount) {
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}