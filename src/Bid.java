import java.time.LocalDateTime;
public class Bid {
    private Long bidderId;
    private Integer amount;
    private LocalDateTime timestamp;
    public Bid(Long bidderId, Integer amount) {
        this.bidderId = bidderId;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
}