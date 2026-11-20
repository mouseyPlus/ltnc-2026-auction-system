package com.auction;

import com.auction.enums.AuctionStatus;
import com.auction.exceptions.AuctionClosedException;
import com.auction.exceptions.InvalidBidException;
import com.auction.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
class AuctionTest {
    private Auction auction;
    @BeforeEach
    void setup() {
        auction = new Auction(
                1L,
                100L,
                10L,
                1_000_000L,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                AuctionStatus.RUNNING
        );
    }
    @Test
    void shouldAcceptValidBid() {
        Bid bid = new Bid(
                1L,
                1L,
                20L,
                1_010_000L,
                Instant.now());
        assertDoesNotThrow(() -> auction.placeBid(bid));
    }
    @Test
    void shouldRejectSellerBid() {
        Bid sellerBid = new Bid(
                1L,
                1L,
                10L,
                1_010_000L,
                Instant.now()
        );
        assertThrows(
                InvalidBidException.class, () -> auction.placeBid(sellerBid)
        );
    }
    @Test
    void shouldRejectWrongAuctionBid() {
        Bid wrongAuctionBid = new Bid(
                1L,
                999L,
                20L,
                1_010_000L,
                Instant.now()
        );
        assertThrows(InvalidBidException.class, () -> auction.placeBid(wrongAuctionBid)
        );
    }
    @Test
    void shouldRejectClosedAuction() {
        auction.finishAuction();
        Bid bid = new Bid(
                1L,
                1L,
                20L,
                1_010_000L,
                Instant.now()
        );
        assertThrows(
                AuctionClosedException.class,
                () -> auction.placeBid(bid)
        );
    }
    @Test
    void shouldRejectExpiredAuction() {
        Auction expiredAuction = new Auction(
                1L,
                100L,
                10L,
                1_000_000L,
                Instant.now().minusSeconds(7200),
                Instant.now().minusSeconds(3600),
                AuctionStatus.RUNNING
        );
        Bid bid = new Bid(
                1L,
                1L,
                20L,
                1_010_000L,
                Instant.now()
        );
        assertThrows(
                AuctionClosedException.class,
                () -> expiredAuction.placeBid(bid)
        );
    }
    @Test
    void shouldRejectBidBelowMinimumIncrement() {
        Bid bid = new Bid(
                1L,
                1L,
                20L,
                1_005_000L,
                Instant.now()
        );
        assertThrows(
                InvalidBidException.class,
                () -> auction.placeBid(bid)
        );
    }
}