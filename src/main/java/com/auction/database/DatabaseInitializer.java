package com.auction.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Loads the database schema and initializes the auction database. */
public final class DatabaseInitializer {

  private static final String SCHEMA_RESOURCE = "/db/schema.sql";
  private static final int CURRENT_SCHEMA_VERSION = 1;
  private static final Set<String> APPLICATION_TABLES =
      Set.of("USERS", "USER_ROLES", "ITEMS", "AUCTIONS", "BIDS");
  private static final Map<String, Set<String>> REQUIRED_COLUMNS =
      Map.of(
          "USERS",
          Set.of(
              "user_id",
              "username",
              "email",
              "phone",
              "password_hash",
              "display_name",
              "created_at_utc",
              "is_active"),
          "USER_ROLES",
          Set.of("user_id", "role"),
          "ITEMS",
          Set.of(
              "item_id",
              "owner_id",
              "name",
              "description",
              "category",
              "created_at_utc",
              "is_active"),
          "AUCTIONS",
          Set.of(
              "auction_id",
              "item_id",
              "seller_id",
              "starting_price_vnd",
              "start_time_utc",
              "end_time_utc",
              "status"),
          "BIDS",
          Set.of(
              "bid_id",
              "auction_id",
              "bidder_id",
              "amount_vnd",
              "bid_time_utc"));

  private DatabaseInitializer() {}

  /**
   * Initializes the database using a newly opened shared connection.
   *
   * @throws SQLException if the connection or schema initialization fails
   */
  public static void initialize() throws SQLException {
    try (Connection connection = DatabaseConnection.getConnection()) {
      initialize(connection);
    }
  }

  static void initialize(Connection connection) throws SQLException {
    if (connection == null) {
      throw new SQLException("Database connection must not be null");
    }

    int version = getUserVersion(connection);
    if (version > CURRENT_SCHEMA_VERSION) {
      throw incompatibleDatabase(
          "Unsupported schema version "
              + version
              + "; this application supports version "
              + CURRENT_SCHEMA_VERSION);
    }

    Set<String> applicationTables = findApplicationTables(connection);
    if (applicationTables.isEmpty()) {
      if (version == 0 && findAllTables(connection).isEmpty()) {
        initializeFreshDatabase(connection);
        return;
      }
      throw incompatibleDatabase(
          "existing database is missing all required application tables");
    }

    if (!applicationTables.contains("USERS")) {
      throw incompatibleDatabase("existing database is missing the required USERS table");
    }

    Set<String> userColumns = findColumns(connection, "USERS");
    if (!userColumns.contains("phone")) {
      throw new SQLException(
          "USERS table is missing the required phone column. "
              + "An explicit data migration is required; "
              + "the database schema and user_version have not been modified.");
    }

    validateCurrentSchema(connection);

    if (version == 0) {
      updateUserVersion(connection, CURRENT_SCHEMA_VERSION);
    } else if (version != CURRENT_SCHEMA_VERSION) {
      throw incompatibleDatabase(
          "unsupported schema version " + version);
    }
  }

  private static void initializeFreshDatabase(Connection connection) throws SQLException {
    boolean originalAutoCommit = connection.getAutoCommit();
    try {
      connection.setAutoCommit(false);
      String schemaSql = loadSchema();
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(schemaSql);
      }

      validateCurrentSchema(connection);
      setUserVersion(connection, CURRENT_SCHEMA_VERSION);
      connection.commit();
    } catch (SQLException exception) {
      rollback(connection, exception);
      throw exception;
    } finally {
      connection.setAutoCommit(originalAutoCommit);
    }
  }

  private static int getUserVersion(Connection connection) throws SQLException {
    try (Statement statement = connection.createStatement();
        var resultSet = statement.executeQuery("PRAGMA user_version")) {
      if (!resultSet.next()) {
        throw incompatibleDatabase("unable to read PRAGMA user_version");
      }
      return resultSet.getInt(1);
    }
  }

  private static Set<String> findApplicationTables(Connection connection)
      throws SQLException {
    Set<String> tables = new LinkedHashSet<>();
    String sql =
        "SELECT name FROM sqlite_master "
            + "WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
    try (Statement statement = connection.createStatement();
        var resultSet = statement.executeQuery(sql)) {
      while (resultSet.next()) {
        String name = resultSet.getString("name");
        if (APPLICATION_TABLES.contains(name)) {
          tables.add(name);
        }
      }
    }
    return tables;
  }

  private static Set<String> findAllTables(Connection connection)
      throws SQLException {
    Set<String> tables = new LinkedHashSet<>();
    String sql =
        "SELECT name FROM sqlite_master "
            + "WHERE type = 'table' AND name NOT LIKE 'sqlite_%'";
    try (Statement statement = connection.createStatement();
        var resultSet = statement.executeQuery(sql)) {
      while (resultSet.next()) {
        tables.add(resultSet.getString("name"));
      }
    }
    return tables;
  }

  private static Set<String> findColumns(Connection connection, String tableName)
      throws SQLException {
    Set<String> columns = new LinkedHashSet<>();
    try (Statement statement = connection.createStatement();
        var resultSet =
            statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
      while (resultSet.next()) {
        columns.add(resultSet.getString("name"));
      }
    }
    return columns;
  }

  private static void validateCurrentSchema(Connection connection) throws SQLException {
    Set<String> tables = findApplicationTables(connection);
    Set<String> missingTables = new LinkedHashSet<>(APPLICATION_TABLES);
    missingTables.removeAll(tables);
    if (!missingTables.isEmpty()) {
      throw incompatibleDatabase(
          "existing database is missing required application tables: " + missingTables);
    }

    for (Map.Entry<String, Set<String>> entry : REQUIRED_COLUMNS.entrySet()) {
      Set<String> actualColumns = findColumns(connection, entry.getKey());
      Set<String> missingColumns =
          new LinkedHashSet<>(entry.getValue());
      missingColumns.removeAll(actualColumns);
      if (!missingColumns.isEmpty()) {
        throw incompatibleDatabase(
            "table "
                + entry.getKey()
                + " is missing required columns: "
                + missingColumns);
      }
    }

    validatePhoneConstraint(connection);
    validateUniqueColumn(connection, "username");
    validateUniqueColumn(connection, "email");
    validateRequiredNonNullUsersColumns(connection);
    validateNotNullColumns(
        connection,
        "USER_ROLES",
        Set.of("user_id", "role"));
    validateNotNullColumns(
        connection,
        "ITEMS",
        Set.of("owner_id"));
    validateNotNullColumns(
        connection,
        "AUCTIONS",
        Set.of("item_id", "seller_id"));
    validateNotNullColumns(
        connection,
        "BIDS",
        Set.of("auction_id", "bidder_id"));
    validatePrimaryKey(connection, "USERS", Set.of("user_id"));
    validateCompositePrimaryKey(connection, "USER_ROLES", Set.of("user_id", "role"));
    validateCompositePrimaryKey(connection, "ITEMS", Set.of("item_id"));
    validateCompositePrimaryKey(connection, "AUCTIONS", Set.of("auction_id"));
    validateCompositePrimaryKey(connection, "BIDS", Set.of("bid_id"));
    validateUsersChecks(connection);
    validateUserRolesConstraints(connection);
    validateItemsChecks(connection);
    validateAuctionsChecks(connection);
    validateActiveAuctionIndex(connection);
    validateForeignKey(
        connection, "ITEMS", "owner_id", "USERS", "user_id");
    validateForeignKey(
        connection, "AUCTIONS", "item_id", "ITEMS", "item_id");
    validateForeignKey(
        connection, "AUCTIONS", "seller_id", "USERS", "user_id");
    validateForeignKey(
        connection, "BIDS", "auction_id", "AUCTIONS", "auction_id");
    validateForeignKey(
        connection, "BIDS", "bidder_id", "USERS", "user_id");
  }

  private static void validatePrimaryKey(
      Connection connection,
      String tableName,
      Set<String> requiredColumns) throws SQLException {
    validateCompositePrimaryKey(connection, tableName, requiredColumns);
  }

  private static void validateCompositePrimaryKey(
      Connection connection,
      String tableName,
      Set<String> requiredColumns) throws SQLException {
    Set<String> primaryKeyColumns = new LinkedHashSet<>();
    try (Statement statement = connection.createStatement();
        var columns =
            statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
      while (columns.next()) {
        if (columns.getInt("pk") > 0) {
          primaryKeyColumns.add(columns.getString("name"));
        }
      }
    }
    if (!primaryKeyColumns.equals(requiredColumns)) {
      throw incompatibleDatabase(
          "table "
              + tableName
              + " does not have the required primary key "
              + requiredColumns);
    }
  }

  private static void validateUsersChecks(Connection connection) throws SQLException {
    String sql = findCreateTableSql(connection, "USERS");
    String normalized = normalizeSql(sql);
    if (!normalized.contains(
        "is_activeintegernotnulldefault1check(is_activein(0,1))")) {
      throw incompatibleDatabase(
          "USERS.is_active does not have the required CHECK constraint");
    }
  }

  private static void validateUserRolesConstraints(Connection connection)
      throws SQLException {
    String sql = findCreateTableSql(connection, "USER_ROLES");
    String normalized = normalizeSql(sql);
    if (!normalized.contains(
        "roletextnotnullcheck(rolein('bidder','seller','admin'))")) {
      throw incompatibleDatabase(
          "USER_ROLES.role does not have the required CHECK constraint");
    }

    validateForeignKey(
        connection,
        "USER_ROLES",
        "user_id",
        "USERS",
        "user_id");
    validateUserRolesForeignKeyAction(connection);
  }

  private static void validateUserRolesForeignKeyAction(Connection connection)
      throws SQLException {
    try (Statement statement = connection.createStatement();
        var foreignKeys =
            statement.executeQuery("PRAGMA foreign_key_list(USER_ROLES)")) {
      while (foreignKeys.next()) {
        if (foreignKeys.getString("table").equalsIgnoreCase("USERS")
            && foreignKeys.getString("from").equalsIgnoreCase("user_id")
            && foreignKeys.getString("to").equalsIgnoreCase("user_id")) {
          if (!"RESTRICT".equalsIgnoreCase(
              foreignKeys.getString("on_delete"))) {
            throw incompatibleDatabase(
                "USER_ROLES.user_id foreign key must use ON DELETE RESTRICT");
          }
          return;
        }
      }
    }
    throw incompatibleDatabase(
        "USER_ROLES.user_id foreign key to USERS is missing");
  }

  private static void validateItemsChecks(Connection connection) throws SQLException {
    String normalized = normalizeSql(findCreateTableSql(connection, "ITEMS"));
    if (!normalized.contains(
        "categorytextnotnullcheck(categoryin('electronics','art','vehicle'))")) {
      throw incompatibleDatabase(
          "ITEMS.category does not have the required CHECK constraint");
    }
  }

  private static void validateAuctionsChecks(Connection connection) throws SQLException {
    String normalized = normalizeSql(findCreateTableSql(connection, "AUCTIONS"));
    if (!normalized.contains(
        "statustextnotnullcheck(statusin("
            + "'open','running','finished','paid','canceled'))")) {
      throw incompatibleDatabase(
          "AUCTIONS.status does not have the required CHECK constraint");
    }
    if (!normalized.contains("check(end_time_utc>start_time_utc)")) {
      throw incompatibleDatabase(
          "AUCTIONS.end_time_utc > start_time_utc "
              + "CHECK constraint is missing");
    }
  }

  private static void validateActiveAuctionIndex(Connection connection)
      throws SQLException {
    try (Statement statement = connection.createStatement();
        var indexes =
            statement.executeQuery("PRAGMA index_list(AUCTIONS)")) {
      while (indexes.next()) {
        if (!"ux_auctions_active_item".equals(
            indexes.getString("name"))) {
          continue;
        }
        if (indexes.getInt("unique") != 1
            || indexes.getInt("partial") != 1) {
          throw incompatibleDatabase(
              "ux_auctions_active_item must be unique and partial");
        }

        Set<String> columns = new LinkedHashSet<>();
        String indexName = indexes.getString("name");
        try (Statement indexStatement = connection.createStatement();
            var indexColumns =
                indexStatement.executeQuery(
                    "PRAGMA index_info(\""
                        + indexName.replace("\"", "\"\"")
                        + "\")")) {
          while (indexColumns.next()) {
            columns.add(indexColumns.getString("name"));
          }
        }
        if (!columns.equals(Set.of("item_id"))) {
          throw incompatibleDatabase(
              "ux_auctions_active_item must index only item_id");
        }

        String sql = findIndexSql(connection, indexName);
        String normalized = normalizeSql(sql);
        String predicate = extractActiveAuctionPredicate(normalized);
        if (!"statusin('open','running')".equals(predicate)) {
          throw incompatibleDatabase(
              "ux_auctions_active_item has an incompatible predicate");
        }
        return;
      }
    }
    throw incompatibleDatabase(
        "missing required partial unique index "
            + "ux_auctions_active_item");
  }

  private static void validateForeignKey(
      Connection connection,
      String fromTable,
      String fromColumn,
      String toTable,
      String toColumn) throws SQLException {
    boolean found = false;
    try (Statement statement = connection.createStatement();
        var foreignKeys =
            statement.executeQuery("PRAGMA foreign_key_list(" + fromTable + ")")) {
      while (foreignKeys.next()) {
        if (foreignKeys.getString("table").equalsIgnoreCase(toTable)
            && foreignKeys.getString("from").equalsIgnoreCase(fromColumn)
            && foreignKeys.getString("to").equalsIgnoreCase(toColumn)) {
          found = true;
          break;
        }
      }
    }
    if (!found) {
      throw incompatibleDatabase(
          "table "
              + fromTable
              + " is missing foreign key "
              + fromColumn
              + " -> "
              + toTable
              + "."
              + toColumn);
    }
  }

  private static String findCreateTableSql(
      Connection connection,
      String tableName) throws SQLException {
    try (Statement statement = connection.createStatement();
        var resultSet =
            statement.executeQuery(
                "SELECT sql FROM sqlite_master "
                    + "WHERE type = 'table' "
                    + "AND name = '" + tableName.replace("'", "''") + "'")) {
      if (!resultSet.next()) {
        throw incompatibleDatabase("required table " + tableName + " is missing");
      }
      String sql = resultSet.getString("sql");
      if (sql == null) {
        throw incompatibleDatabase(
            "unable to inspect required table " + tableName);
      }
      return sql;
    }
  }

  private static String findIndexSql(
      Connection connection,
      String indexName) throws SQLException {
    try (Statement statement = connection.createStatement();
        var resultSet =
            statement.executeQuery(
                "SELECT sql FROM sqlite_master "
                    + "WHERE type = 'index' "
                    + "AND name = '" + indexName.replace("'", "''") + "'")) {
      if (!resultSet.next()) {
        throw incompatibleDatabase(
            "unable to inspect required index " + indexName);
      }
      String sql = resultSet.getString("sql");
      if (sql == null) {
        throw incompatibleDatabase(
            "unable to inspect required index " + indexName);
      }
      return sql;
    }
  }

  private static String normalizeSql(String sql) {
    return sql.replaceAll("\\s+", " ")
        .replace(" ", "")
        .toLowerCase();
  }

  private static String extractActiveAuctionPredicate(String normalizedSql) {
    int whereIndex = normalizedSql.indexOf("where");
    if (whereIndex < 0) {
      return null;
    }
    String predicate = normalizedSql.substring(whereIndex + "where".length());
    if (predicate.endsWith(";")) {
      predicate = predicate.substring(0, predicate.length() - 1);
    }
    return predicate;
  }

  private static void validateRequiredNonNullUsersColumns(Connection connection)
      throws SQLException {
    Set<String> required =
        Set.of(
            "username",
            "email",
            "phone",
            "password_hash",
            "display_name",
            "created_at_utc",
            "is_active");
    try (Statement statement = connection.createStatement();
        var columns =
            statement.executeQuery("PRAGMA table_info(USERS)")) {
      while (columns.next()) {
        String name = columns.getString(2);
        if (required.contains(name) && columns.getInt(4) != 1) {
          throw incompatibleDatabase(
              "USERS." + name + " is not declared NOT NULL");
        }
      }
    }
  }

  private static void validateNotNullColumns(
      Connection connection,
      String tableName,
      Set<String> requiredColumns) throws SQLException {
    try (Statement statement = connection.createStatement();
        var columns =
            statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
      while (columns.next()) {
        String name = columns.getString("name");
        if (requiredColumns.contains(name) && columns.getInt("notnull") != 1) {
          throw incompatibleDatabase(
              tableName + "." + name + " is not declared NOT NULL");
        }
      }
    }
  }

  private static void validateUniqueColumn(Connection connection, String columnName)
      throws SQLException {
    try (Statement statement = connection.createStatement();
        var indexes = statement.executeQuery("PRAGMA index_list(USERS)")) {
      while (indexes.next()) {
        if (indexes.getInt("unique") != 1) {
          continue;
        }
        if (indexes.getInt("partial") != 0) {
          continue;
        }

        String indexName = indexes.getString("name");
        Set<String> indexedColumns = new LinkedHashSet<>();
        try (Statement indexStatement = connection.createStatement();
            var columns =
                indexStatement.executeQuery(
                    "PRAGMA index_info(\""
                        + indexName.replace("\"", "\"\"")
                        + "\")")) {
          while (columns.next()) {
            indexedColumns.add(columns.getString("name"));
          }
        }
        if (indexedColumns.equals(Set.of(columnName))) {
          return;
        }
      }
    }
    throw incompatibleDatabase(
        "USERS." + columnName + " is missing a full-column UNIQUE constraint");
  }

  private static void validatePhoneConstraint(Connection connection) throws SQLException {
    validateNotNullColumns(connection, "USERS", Set.of("phone"));
    validateUniqueColumn(connection, "phone");
  }

  private static void updateUserVersion(Connection connection, int version)
      throws SQLException {
    boolean originalAutoCommit = connection.getAutoCommit();
    try {
      connection.setAutoCommit(false);
      setUserVersion(connection, version);
      connection.commit();
    } catch (SQLException exception) {
      rollback(connection, exception);
      throw exception;
    } finally {
      connection.setAutoCommit(originalAutoCommit);
    }
  }

  private static void setUserVersion(Connection connection, int version)
      throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute("PRAGMA user_version = " + version);
    }
  }

  private static void rollback(Connection connection, SQLException failure) {
    try {
      connection.rollback();
    } catch (SQLException rollbackFailure) {
      failure.addSuppressed(rollbackFailure);
    }
  }

  private static SQLException incompatibleDatabase(String reason) {
    return new SQLException("Incompatible database schema: " + reason);
  }

  private static String loadSchema() throws SQLException {
    try (InputStream input =
        DatabaseInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
      if (input == null) {
        throw new SQLException("Schema resource not found: " + SCHEMA_RESOURCE);
      }
      return new String(input.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new SQLException("Failed to read schema resource: " + SCHEMA_RESOURCE, exception);
    }
  }
}
