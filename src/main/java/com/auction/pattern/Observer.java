package com.auction.pattern;

import com.auction.model.Bid;

public interface Observer {
    void update(Bid bid);
}
