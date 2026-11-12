-- SQLite foreign keys must also be enabled on every JDBC connection at runtime.
PRAGMA foreign_keys = ON;

-- Stores registered users. Roles are assigned through USER_ROLES.
CREATE TABLE IF NOT EXISTS USERS (
    user_id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    display_name TEXT NOT NULL,
    created_at_utc TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

-- Assigns one or more roles to each user.
CREATE TABLE IF NOT EXISTS USER_ROLES (
    user_id INTEGER NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('BIDDER', 'SELLER', 'ADMIN')),
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES USERS (user_id) ON DELETE RESTRICT
);

-- Stores products and their current owners.
CREATE TABLE IF NOT EXISTS ITEMS (
    item_id INTEGER PRIMARY KEY,
    owner_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    category TEXT NOT NULL CHECK (category IN ('ELECTRONICS', 'ART', 'VEHICLE')),
    created_at_utc TEXT NOT NULL,
    is_active INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    FOREIGN KEY (owner_id) REFERENCES USERS (user_id) ON DELETE RESTRICT
);

-- Stores auction listings and their lifecycle status.
CREATE TABLE IF NOT EXISTS AUCTIONS (
    auction_id INTEGER PRIMARY KEY,
    item_id INTEGER NOT NULL,
    seller_id INTEGER NOT NULL,
    starting_price_vnd INTEGER NOT NULL CHECK (starting_price_vnd >= 0),
    start_time_utc TEXT NOT NULL,
    end_time_utc TEXT NOT NULL,
    status TEXT NOT NULL CHECK (
        status IN ('OPEN', 'RUNNING', 'FINISHED', 'PAID', 'CANCELED')
    ),
    CHECK (end_time_utc > start_time_utc),
    FOREIGN KEY (item_id) REFERENCES ITEMS (item_id) ON DELETE RESTRICT,
    FOREIGN KEY (seller_id) REFERENCES USERS (user_id) ON DELETE RESTRICT
);

-- Stores every accepted bid as an immutable history record.
CREATE TABLE IF NOT EXISTS BIDS (
    bid_id INTEGER PRIMARY KEY,
    auction_id INTEGER NOT NULL,
    bidder_id INTEGER NOT NULL,
    amount_vnd INTEGER NOT NULL CHECK (amount_vnd > 0),
    bid_time_utc TEXT NOT NULL,
    FOREIGN KEY (auction_id) REFERENCES AUCTIONS (auction_id) ON DELETE RESTRICT,
    FOREIGN KEY (bidder_id) REFERENCES USERS (user_id) ON DELETE RESTRICT
);

-- Allows multiple historical auctions while permitting only one OPEN or
-- RUNNING auction for an item at a time.
CREATE UNIQUE INDEX IF NOT EXISTS ux_auctions_active_item
    ON AUCTIONS (item_id)
    WHERE status IN ('OPEN', 'RUNNING');

-- Supports retrieving all historical auctions for an item.
CREATE INDEX IF NOT EXISTS idx_auctions_item_id
    ON AUCTIONS (item_id);

-- Supports owner inventory lookups.
CREATE INDEX IF NOT EXISTS idx_items_owner_id
    ON ITEMS (owner_id);

-- Supports auction listings by lifecycle status and start time.
CREATE INDEX IF NOT EXISTS idx_auctions_status_start_time
    ON AUCTIONS (status, start_time_utc);

-- Supports chronological bid history for an auction.
CREATE INDEX IF NOT EXISTS idx_bids_auction_time
    ON BIDS (auction_id, bid_time_utc, bid_id);

-- Supports a bidder's bid history.
CREATE INDEX IF NOT EXISTS idx_bids_bidder_time
    ON BIDS (bidder_id, bid_time_utc, bid_id);

-- Supports highest-bid lookup, with bid_id providing deterministic tie ordering.
CREATE INDEX IF NOT EXISTS idx_bids_auction_amount
    ON BIDS (auction_id, amount_vnd DESC, bid_time_utc ASC, bid_id ASC);
