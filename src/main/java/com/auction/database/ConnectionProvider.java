package com.auction.database;

import java.sql.Connection;
import java.sql.SQLException;

/** Provides database connections to DAO implementations. */
@FunctionalInterface
public interface ConnectionProvider {

  /**
   * Opens or obtains a database connection.
   *
   * @return an available database connection
   * @throws SQLException if a connection cannot be obtained
   */
  Connection getConnection() throws SQLException;
}
