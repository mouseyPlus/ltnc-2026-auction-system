# UserDAO v1 Test Plan

## Scope

This plan covers the implementation of the `UserDAO` contract documented in
`docs/database/user-dao-v1-contract.md`. Tests use `DatabaseTestSupport` with
one isolated temporary file for each test method.

The test implementation must:

- Use `DatabaseTestSupport` and constructor-inject `support::openConnection`
  as the `ConnectionProvider`.
- Open and close connections and statements with try-with-resources.
- Never open or modify `data/auction.db`; use `DatabaseTestSupport` temporary files.
- Use prepared statements for all identifier and value lookups.
- Not repeat database constraints already covered thoroughly by `SchemaTest`.
- Test DAO behavior once the OOP `User` and `Role` types exist.

The entity fixture must represent the agreed mapping:

- `Long id`
- `String username`
- `String email`
- `String phone`
- `String passwordHash`
- `String displayName`
- `Instant createdAtUtc`
- `boolean isActive`
- `Set<Role> roles`

## P0 - Essential DAO Tests

### User Creation

#### `createUserPersistsUserAndRoles`

- **Setup:** Fresh temporary database; create a valid `User` with
  `BIDDER` and `SELLER` roles.
- **Action:** Call `createUser(user)`.
- **Expected result:** Returns a positive generated ID; `USERS` contains exactly
  one matching row; `USER_ROLES` contains exactly both supplied roles.
- **Database assertions:** Match ID, all mapped columns, `is_active = 1`,
  `created_at_utc` round-trips to the supplied `Instant`, and both role rows.
- **Level:** DAO.

#### `createUserAssignsGeneratedIdAndReconstructsRoles`

- **Setup:** Valid user with ID `null` or not yet assigned and roles
  `BIDDER`, `SELLER`, `ADMIN`.
- **Action:** Call `createUser`, then `findById(returnedId)`.
- **Expected result:** Returned ID is stored in the entity; lookup returns the
  complete user and a complete `Set<Role>`.
- **Database assertions:** The generated ID equals the `USERS.user_id` and
  every `USER_ROLES.user_id` reference.
- **Level:** DAO.

#### `createUserRejectsNullUser`

- **Setup:** Empty temporary database.
- **Action:** Call `createUser(null)`.
- **Expected result:** Throws `IllegalArgumentException` with no SQL execution.
- **Database assertions:** No `USERS` or `USER_ROLES` rows.
- **Level:** DAO/domain validation.

#### `createUserRejectsNullRoles`

- **Setup:** Valid user with `roles = null`.
- **Action:** Call `createUser`.
- **Expected result:** Throws `IllegalArgumentException`; no SQL insertion.
- **Database assertions:** No user or role rows.
- **Level:** DAO.

#### `createUserRejectsEmptyRoles`

- **Setup:** Valid user with `roles = empty set`.
- **Action:** Call `createUser`.
- **Expected result:** Throws `IllegalArgumentException`; no SQL insertion.
- **Database assertions:** No user or role rows.
- **Level:** DAO.

#### `createUserRejectsPreassignedId`

- **Setup:** Valid user with a positive ID already assigned.
- **Action:** Call `createUser(user)`.
- **Expected result:** Throws `IllegalArgumentException` and performs no insert.
- **Database assertions:** No `USERS` or `USER_ROLES` rows are added.
- **Level:** DAO/domain validation.

#### `createUserRollsBackWhenRoleInsertFails`

- **Setup:** Empty database and valid user with `BIDDER` and `ADMIN`.
  Create this deterministic trigger before calling the DAO:

```sql
CREATE TRIGGER fail_admin_role
BEFORE INSERT ON USER_ROLES
WHEN NEW.role = 'ADMIN'
BEGIN
  SELECT RAISE(ABORT, 'forced ADMIN role insertion failure');
END;
```

- **Action:** Call `createUser`.
- **Expected result:** `SQLException` is propagated; the transaction rolls back
  and no partial data remains. The original User object's ID remains null.
- **Database assertions:** `USERS` row count is zero and `USER_ROLES` row count
  is zero. The trigger itself may remain in the temporary database.
- **Level:** DAO transaction test.

### Uniqueness

#### `createUserRejectsDuplicateUsername`

- **Setup:** Insert user A with unique username.
- **Action:** Create user B with the same username and unique email and phone.
- **Expected result:** Propagates `SQLException`; no second user or role row.
- **Database assertions:** Original row and roles remain unchanged; total user
  count remains one.
- **Level:** DAO.

#### `createUserRejectsDuplicateEmail`

- **Setup:** Insert user A with unique email.
- **Action:** Create user B with unique username and phone but the same email.
- **Expected result:** Propagates `SQLException`; no partial records.
- **Database assertions:** Original row remains and user/role counts are unchanged.
- **Level:** DAO.

#### `createUserRejectsDuplicatePhone`

- **Setup:** Insert user A with unique phone.
- **Action:** Create user B with unique username and email but the same phone.
- **Expected result:** Propagates `SQLException`; no partial records.
- **Database assertions:** Original row remains and user/role counts are unchanged.
- **Level:** DAO.

### User Lookup

#### `findByIdReturnsExistingUserWithAllFieldsAndRoles`

- **Setup:** Create an active or inactive user with at least `BIDDER` and
  `SELLER`; verify all database fields.
- **Action:** Call `findById(generatedId)`.
- **Expected result:** `Optional` contains the complete mapped user and full role set.
- **Database assertions:** Compare ID, username, email, phone, password hash,
  display name, UTC `Instant`, active state, and roles.
- **Level:** DAO.

#### `findByIdReturnsEmptyForUnknownId`

- **Setup:** Empty or otherwise initialized temporary database.
- **Action:** Call `findById(999999)`.
- **Expected result:** `Optional.empty()`.
- **Database assertions:** No rows are inserted or modified.
- **Level:** DAO.

#### `findByIdRejectsNullOrInvalidId`

- **Setup:** Initialized database.
- **Action:** Call `findById(null)`, `0`, and `-1` as separate cases.
- **Expected result:** Null/non-positive IDs throw `IllegalArgumentException`.
  This behavior must be implemented consistently by the DAO.
- **Database assertions:** No data changes.
- **Level:** DAO/domain validation.

#### `findByIdentifierMatchesUsername`

- **Setup:** Insert one user.
- **Action:** Call `findByIdentifier(user.username)`.
- **Expected result:** Exact matching user is returned with all fields and roles.
- **Database assertions:** No duplicate or extra rows are returned.
- **Level:** DAO.

#### `findByIdentifierMatchesEmail`

- **Setup:** Insert one user.
- **Action:** Call `findByIdentifier(user.email)`.
- **Expected result:** Exact matching user is returned.
- **Database assertions:** Returned ID and fields match the database row.
- **Level:** DAO.

#### `findByIdentifierMatchesPhone`

- **Setup:** Insert one user.
- **Action:** Call `findByIdentifier(user.phone)`.
- **Expected result:** Exact matching user is returned.
- **Database assertions:** Returned ID and fields match the database row.
- **Level:** DAO.

#### `findByIdentifierReturnsEmptyForUnknownValue`

- **Setup:** Initialized database with users.
- **Action:** Search for a unique nonexistent identifier.
- **Expected result:** `Optional.empty()`.
- **Database assertions:** No data changes.
- **Level:** DAO.

#### `findByIdAndIdentifierReturnInactiveUsers`

- **Setup:** Create or update a user to `is_active = 0`.
- **Action:** Find it by ID and identifier.
- **Expected result:** Both lookups return the inactive user.
- **Database assertions:** `is_active = 0` and roles are preserved.
- **Level:** DAO.

#### `findByIdentifierDetectsCrossColumnAmbiguity`

- **Setup:** User A has `username = "shared@example.com"`; user B has
  `email = "shared@example.com"`, with all identifiers unique within their
  respective columns.
- **Action:** Call `findByIdentifier("shared@example.com")`.
- **Expected result:** Throws unchecked
  `com.auction.database.UserIdentifierAmbiguityException`
  and never returns either user arbitrarily.
- **Database assertions:** Both rows remain unchanged and no password hash is
  included in the exception.
- **Level:** DAO lookup plus service-level ambiguity handling.

### User Updates

#### `updateUserUpdatesOnlyEditableFields`

- **Setup:** Insert a user with `BIDDER, SELLER`, password hash, timestamp,
  and active state.
- **Action:** Change username, email, phone, and display name only; leave
  immutable fields unchanged.
- **Expected result:** Returns `true`.
- **Database assertions:** The four editable columns change; ID, password hash,
  timestamp, active state, and role rows remain unchanged.
- **Level:** DAO.

#### `updateUserRejectsDuplicateValue`

- **Setup:** Insert two users with distinct identifiers.
- **Action:** Change the second user's username, email, or phone to a value owned
  by the first user.
- **Expected result:** Propagates `SQLException`; returns no success result.
- **Database assertions:** Snapshot of the second row is unchanged, including
  display name and password hash.
- **Level:** DAO.

#### `updateUserUnknownIdReturnsFalse`

- **Setup:** Empty or initialized database.
- **Action:** Call `updateUser` with a nonexisting ID.
- **Expected result:** Returns `false`; no row is inserted.
- **Database assertions:** User and role counts remain unchanged.
- **Level:** DAO.

#### `updateUserRejectsNullUser`

- **Setup:** Initialized temporary database.
- **Action:** Call `updateUser(null)`.
- **Expected result:** Throws `IllegalArgumentException` with no SQL execution.
- **Database assertions:** No rows or role records are changed.
- **Level:** DAO.

#### `updateUserCannotChangeProtectedFields`

- **Setup:** Insert one user.
- **Action:** At the entity boundary, verify that an ordinary setter cannot
  replace a persisted ID. A `guarded assignGeneratedId` operation may assign
  a positive ID only once. Separately, supply an entity with changed password
  hash, created timestamp, active state, or roles and call `updateUser`.
- **Expected result:** Protected fields remain unchanged in the database.
  The DAO's SQL `WHERE user_id = ?` uses the original persisted ID; `updateUser`
  never writes the primary key.
- **Database assertions:** Query all protected database fields and role rows.
- **Level:** DAO; entity immutability may be enforced in the domain layer.

### Role Updates

#### `updateRolesReplacesAllExistingRolesAtomically`

- **Setup:** Insert user with `BIDDER, SELLER`.
- **Action:** Call `updateRoles(id, {SELLER, ADMIN})`.
- **Expected result:** Returns `true`.
- **Database assertions:** Exactly `SELLER, ADMIN` remain; `BIDDER` and duplicate
  rows are absent.
- **Level:** DAO.

#### `updateRolesRejectsNullRoleSet`

- **Setup:** Insert a user.
- **Action:** Call `updateRoles(id, null)`.
- **Expected result:** Throws `IllegalArgumentException`; roles are unchanged.
- **Database assertions:** Original role set remains complete.
- **Level:** DAO.

#### `updateRolesRejectsEmptyRoleSet`

- **Setup:** Insert a user.
- **Action:** Call `updateRoles(id, emptySet)`.
- **Expected result:** Throws `IllegalArgumentException`; roles are unchanged.
- **Database assertions:** Original role set remains complete.
- **Level:** DAO.

#### `updateRolesUnknownUserReturnsFalse`

- **Setup:** Empty database or no matching user.
- **Action:** Call `updateRoles(999999, {SELLER})`.
- **Expected result:** Returns `false`; no role rows are created.
- **Database assertions:** `USER_ROLES` remains empty.
- **Level:** DAO.

#### `updateRolesRollsBackWhenRoleInsertFails`

- **Setup:** Insert user with `BIDDER, SELLER`. Create a trigger that aborts
  insertion of `ADMIN`:

```sql
CREATE TRIGGER fail_admin_role_update
BEFORE INSERT ON USER_ROLES
WHEN NEW.role = 'ADMIN'
BEGIN
  SELECT RAISE(ABORT, 'forced role update failure');
END;
```

- **Action:** Call `updateRoles(id, {SELLER, ADMIN})`.
- **Expected result:** Propagates `SQLException`; deletion and insertion are
  rolled back.
- **Database assertions:** Original `BIDDER, SELLER` role set remains; no partial
  `SELLER` or `ADMIN` replacement state exists.
- **Level:** DAO transaction test.

### Deactivation

#### `deactivateUserSetsInactive`

- **Setup:** Insert active user with roles.
- **Action:** Call `deactivateUser(id)`.
- **Expected result:** Returns `true`.
- **Database assertions:** `is_active = 0`; user, password hash, profile fields,
  timestamp, and role rows remain.
- **Level:** DAO.

#### `deactivateUserIsSafeWhenRepeated`

- **Setup:** Insert and deactivate a user.
- **Action:** Call `deactivateUser(id)` again.
- **Expected result:** Returns `true`; the operation is idempotent and never
  deletes data.
- **Database assertions:** `is_active = 0`; exactly one user and unchanged roles.
- **Level:** DAO.

#### `deactivateUserUnknownIdReturnsFalse`

- **Setup:** Empty database or no matching ID.
- **Action:** Call `deactivateUser(999999)`.
- **Expected result:** Returns `false`; no row is inserted.
- **Database assertions:** User and role counts remain unchanged.
- **Level:** DAO.

### Persistence Safety

#### `daoNeverPhysicallyDeletesUser`

- **Setup:** Insert active user with role rows.
- **Action:** Deactivate and update the user and roles normally.
- **Expected result:** The user row remains; `updateRoles` changes role rows,
  while deactivation only changes `is_active`.
- **Database assertions:** No JUnit assertion can prove source-level absence of
  `DELETE FROM USERS`; this test verifies runtime preservation only.
- **Level:** Source-code review invariant plus JUnit for runtime behavior.

#### `connectionAndStatementResourcesAreClosed`

- **Setup:** Inject a custom `ConnectionProvider` backed by a trackable JDBC
  connection, or inspect a connection after one DAO operation.
- **Action:** Execute lookup or update methods.
- **Expected result:** DAO closes its own statement/result set and connection;
  test connection is closed or rejected for reuse.
- **Database assertions:** Database remains intact.
- **Level:** DAO/resource management test.

Connection ownership requirement:

- `UserDAO` obtains the connection through `ConnectionProvider`.
- It owns and closes that connection.
- It closes statements and result sets.
- It uses one connection for each complete create, update, role, lookup, or
  deactivation operation.
- It manages its own transaction boundaries and does not share connections or
  transactions with unrelated caller operations.

## P1 - Important Edge Cases

### Identifier and Input Handling

#### `findByIdentifierRejectsNullOrBlank`

- **Setup:** Initialized database.
- **Action:** Call null and whitespace-only identifiers.
- **Expected result:** Throws `IllegalArgumentException`; no SQL query.
- **Database assertions:** No data changes.
- **Level:** DAO/domain validation.

#### `createUserRejectsNullOrBlankRequiredFields`

- **Setup:** Valid role set.
- **Action:** Set username, email, phone, password hash, or display name to null or
  whitespace, and set no created timestamp.
- **Expected result:** This is a domain/service validation test unless the DAO
  contract explicitly adopts it. The domain or service rejects invalid input
  before SQL; it must not become an undocumented mandatory UserDAO behavior.
  Null created timestamp is not stored.
- **Database assertions:** No user or role rows.
- **Level:** Primarily domain/service, with DAO defensive validation.

#### `updateUserRejectsNullOrBlankEditableFields`

- **Setup:** Existing user.
- **Action:** Set username, email, phone, or display name to null or blank.
- **Expected result:** This is a domain/service field-format rule and is not
  mandatory UserDAO behavior unless the
  entity contract explicitly adds DAO validation. The service/domain layer
  rejects the values before calling the DAO; no partial update occurs.
- **Database assertions:** Original row remains.
- **Level:** Domain/service validation plus DAO.

### Invalid IDs and Roles

#### `invalidDaoIdsAreRejected`

- **Setup:** Initialized database.
- **Action:** Call lookup, update, role update, and deactivation with null, zero,
  or negative IDs.
- **Expected result:** Consistent `IllegalArgumentException`; no SQL query or row
  changes.
- **Database assertions:** Database remains unchanged.
- **Level:** DAO/domain validation.

#### `unsupportedRoleCannotBePersisted`

- **Setup:** Empty database.
- **Action:** Supply an unsupported role through raw entity construction or
  attempt a direct `USER_ROLES` insert with `"OWNER"`.
- **Expected result:** Domain enum prevents unsupported values; direct DAO or
  JDBC persistence propagates a `SQLException` due to the role `CHECK`.
- **Database assertions:** No invalid role row exists.
- **Level:** Domain and DAO.

### Timestamp and Password

#### `createdAtInstantRoundTripsExactly`

- **Setup:** Valid user with a known UTC `Instant`, for example
  `Instant.parse("2026-01-01T12:34:56Z")`.
- **Action:** Create and find the user.
- **Expected result:** Reconstructed `Instant` equals the original value.
- **Database assertions:** Stored text is canonical UTC text.
- **Level:** DAO plus domain mapping.

#### `passwordHashIsStoredUnchanged`

- **Setup:** User with a synthetic already-hashed value such as
  `"pbkdf2:test-only-hash"`.
- **Action:** Create and find the user.
- **Expected result:** `passwordHash` exactly equals the supplied value.
- **Database assertions:** No plaintext hashing or transformation occurred.
- **Level:** DAO.

### Security and Provider Failures

#### `identifierLookupIsSafeFromSqlInjection`

- **Setup:** Initialized database.
- **Action:** Search with values containing quotes and SQL such as
  `' OR 1 = 1 --`.
- **Expected result:** No match is returned unless the literal text is a real
  identifier; no extra rows are returned.
- **Database assertions:** Row count and data remain unchanged.
- **Level:** DAO security test.

#### `connectionProviderSQLExceptionPropagates`

- **Setup:** Create a `ConnectionProvider` lambda that throws a deterministic
  `SQLException`.
- **Action:** Call a representative operation such as `findById(1)`.
- **Expected result:** The same `SQLException` is propagated; no fallback
  connection is used and no false result is returned.
- **Database assertions:** Temporary database is unchanged.
- **Level:** DAO/provider test.

## Required Test Fixtures and Helpers

The DAO test class should provide:

- `@TempDir Path temporaryDirectory`.
- `DatabaseTestSupport support = new DatabaseTestSupport(tempDirectory)`.
- `ConnectionProvider connectionProvider = support::openConnection`.
- A factory that creates a valid `User` fixture with unique values per test.
- A helper to insert a user directly for lookup/update setup.
- A helper to read a user row and assert all mapped fields.
- A helper to read `Set<Role>` for a user.
- A helper to count `USERS` and `USER_ROLES`.
- A helper to create or drop deterministic SQLite triggers.
- A helper to use a separate fresh connection to verify cross-connection visibility.

Recommended minimal `UserDAOTest` structure:

```java
@TempDir
Path temporaryDirectory;

@BeforeEach
void setUp() throws Exception {
  support = new DatabaseTestSupport(temporaryDirectory);
  dao = new UserDAO(support::openConnection);
}
```

The exact concrete DAO and entity constructors are intentionally implementation
dependent until the OOP implementation is merged.

## Minimal First-Implementation Suite

Implement these tests first:

1. `createUserPersistsUserAndRoles`
2. `createUserAssignsGeneratedIdAndReconstructsRoles`
3. `createUserRejectsNullUser`
4. `createUserRejectsNullRoles`
5. `createUserRejectsEmptyRoles`
6. `createUserRejectsPreassignedId`
7. `createUserRollsBackWhenRoleInsertFails`
8. `createUserRejectsDuplicateUsername`
9. `createUserRejectsDuplicateEmail`
10. `createUserRejectsDuplicatePhone`
11. `findByIdReturnsExistingUserWithAllFieldsAndRoles`
12. `findByIdReturnsEmptyForUnknownId`
13. `findByIdentifierMatchesUsername`
14. `findByIdentifierMatchesEmail`
15. `findByIdentifierMatchesPhone`
16. `findByIdentifierDetectsCrossColumnAmbiguity`
17. `findByIdAndIdentifierReturnInactiveUsers`
18. `updateUserUpdatesOnlyEditableFields`
19. `updateUserRejectsNullUser`
20. `updateUserRejectsDuplicateValue`
21. `updateRolesReplacesAllExistingRolesAtomically`
22. `updateRolesRollsBackWhenRoleInsertFails`
23. `updateRolesUnknownUserReturnsFalse`
24. `deactivateUserSetsInactive`
25. `deactivateUserIsSafeWhenRepeated`
26. `deactivateUserUnknownIdReturnsFalse`

## Additional Later Tests

Add after the first implementation is stable:

- Bulk lookup/performance tests, if the DAO later exposes them.
- Concurrent role replacement and concurrent profile updates.
- Concurrent duplicate identifier races.
- Connection provider returning the same connection versus fresh connections.
- Identifier normalization and case policy tests.
- Controlled password-update or password-separation tests.
- Repeated rollback after several trigger failures.
- Full relation preservation if ITEMS, AUCTIONS, or BIDS are added to setup.

## Acceptance Criteria

UserDAO v1 is complete when:

- All P0 tests pass.
- SQL constraints propagate as `SQLException`.
- Every lookup reconstructs all mapped fields and the complete role set.
- Creation and role replacement are atomic.
- Unknown `updateUser`, `updateRoles`, and `deactivateUser` IDs return `false`
  as applicable; unknown `findById` returns `Optional.empty()`.
- Cross-column identifiers throw `UserIdentifierAmbiguityException`.
- Null or empty role sets throw `IllegalArgumentException`.
- Null/non-positive IDs throw `IllegalArgumentException`.
- Null/blank identifiers throw `IllegalArgumentException`.
- Inactive users are returned by lookups.
- `updateUser` changes only the four allowed fields.
- Password hash and created timestamp are never modified by profile updates.
- No method physically deletes a user.
- Foreign-key enforcement is enabled through every temporary test connection.
- JUnit and Google Checkstyle pass under `mvn clean verify`.
