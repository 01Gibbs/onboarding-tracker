# Java idioms

Core Java that the Accounts API relies on, and that is easy to get wrong without noticing, especially if an AI assistant wrote your Onboarding Tracker. Each item has a rule, a short example from the course code, and the day where it matters. New words: [glossary](glossary.md).

| # | Idiom | Rule in one line | Day |
|---|---|---|---|
| 1 | [`BigDecimal` for money](#bigdecimal-for-money) | Money is `BigDecimal` made from a String; compare with `compareTo`; round with `HALF_EVEN` | 1, 2, 19, 20–23 |
| 2 | [`Optional`](#optional) | `orElseThrow(...)`, never `.get()` | 1, 2 |
| 3 | [Records vs classes](#records-vs-classes) | Records for DTOs; classes for JPA entities | 0, 1, 6 |
| 4 | [`==` vs `.equals`](#equals) | `.equals` for objects (Strings, `Long`, `BigDecimal`…); `==` only for primitives | 2, 15 |
| 5 | [`List.of` is immutable](#immutable-collections) | `List.of`, `Set.of`, `Map.of` and `.toList()` can't be changed | 3 |
| 6 | [Streams](#streams) | `stream().filter(...).map(...).toList()` to transform a list | 6 |
| 7 | [`java.time`](#java-time) | `LocalDate` for dates, `Instant` for moments; never `java.util.Date` | 0 |
| 8 | [Checked vs unchecked exceptions](#exceptions) | Our exceptions extend `RuntimeException`; `@Transactional` rolls back on those | 1, 14, 19 |
| 9 | [`final` and constructor injection](#constructor-injection) | Dependencies arrive through the constructor into `final` fields | 3 |

---

<a id="bigdecimal-for-money"></a>
## 1. `BigDecimal` for money

**Rule:** money is `BigDecimal`, never `double` or `float`. Build it from a **String**. Compare amounts with `compareTo`, not `equals`. When you must drop digits, `setScale(2, RoundingMode.HALF_EVEN)`.

```java
new BigDecimal(0.1)      // 0.1000000000000000055511151231257827021181583404541015625: double is binary
new BigDecimal("0.1")    // 0.1: exact
new BigDecimal("2.0").equals(new BigDecimal("2.00"))         // false: equals also compares the scale
new BigDecimal("2.0").compareTo(new BigDecimal("2.00")) == 0 // true: same amount
new BigDecimal("2.345").setScale(2, RoundingMode.HALF_EVEN)  // 2.34 (ties go to the even digit; 2.355 → 2.36)
if (balance.compareTo(amount) < 0) { throw new InsufficientFundsException(id); } // Account.withdraw
```

- **Where:** `Account.balance` (`precision = 19, scale = 2`) from Day 1. Day 2's pull request has `double openingBalance` and `new BigDecimal(r.openingBalance())`: two of its six bugs. Tests compare with AssertJ's `isEqualByComparingTo("10.00")` (Day 4). `Account.withdraw` uses `compareTo` (Day 19). Rounding and currency scale (`GBP` 2 decimals, `JPY` 0) arrive with Days 20–23 (money/currency).
- Without a rounding mode, a result that doesn't fit fails loudly: `setScale(2)` on `2.345` throws `ArithmeticException: Rounding necessary`, and `10 ÷ 3` throws `Non-terminating decimal expansion`. That's a feature: decide the rounding explicitly.
- **Read more:** [`BigDecimal`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html), [`RoundingMode`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/RoundingMode.html)

<a id="optional"></a>
## 2. `Optional`: `orElseThrow`, never `.get()`

**Rule:** a method that may find nothing returns `Optional<T>`. Say what "nothing" means with `orElseThrow(...)`; don't call `.get()`.

```java
// Day 2's bug: .get() on an empty Optional throws NoSuchElementException → a 500
return accountRepository.findById(id).get();

// The fix (Day 1 onwards): a 404 via ApiExceptionHandler
return accountRepository.findById(id)
        .orElseThrow(() -> new AccountNotFoundException(id));
```

- **Where:** `AccountService.getById` (Day 1's solution). In tests, a mock returns `Optional.of(account)` or `Optional.empty()` (Day 4).
- Don't use `Optional` for fields or parameters; it's for return values.
- **Read more:** [`Optional`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html)

<a id="records-vs-classes"></a>
## 3. Records vs classes (and why entities aren't records)

**Rule:** use a `record` for data that's created once and read (request and response DTOs). Use a normal class for JPA entities.

```java
public record TransferRequest(@NotNull Long toAccountId, @NotNull @Positive BigDecimal amount) {}
request.amount()   // accessor: no "get" prefix

@Entity public class Account {      // a class: JPA needs these, and a record can't provide them
    protected Account() { }         // 1. a no-argument constructor
    public void withdraw(BigDecimal amount) { balance = balance.subtract(amount); } // 2. fields that change
}
```

- **Why not a record entity:** JPA creates entities with a no-argument constructor, changes their fields (an update is "load, change, save"), and may subclass them as lazy-loading proxies. Records have no no-arg constructor, `final` fields and are themselves `final`.
- **Where:** `CreateAccountRequest`, `UpdateAccountRequest`, `AccountResponse`, `TransferRequest` (Days 1–3, 19). The 2022 service (Day 6) runs on Java 11, which has no records: its DTOs are classes with getters and setters.
- **Read more:** [Oracle: record classes](https://docs.oracle.com/en/java/javase/25/language/records.html), [Spring Data JPA: entity persistence](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html)

<a id="equals"></a>
## 4. `==` vs `.equals`

**Rule:** `==` on objects asks "the same object in memory?". To compare values (`String`, `Long`, `BigDecimal`, your records), use `.equals`. `==` is for primitives (`int`, `long`, `boolean`).

```java
// Day 2's bug: may be false for a String that came from JSON
if (!(c == "GBP" || c == "EUR" || c == "USD")) { … }
// The fix
if (!(c.equals("GBP") || c.equals("EUR") || c.equals("USD"))) { … }
// Day 15: is this the caller's account?
if (!isTeller && !account.getUsername().equals(username)) { … }
```

- `Long` ids are objects too: `a.getId() == b.getId()` happens to work for small numbers (Java caches −128 to 127) and fails above that. Use `.equals` (or `Objects.equals` when either may be `null`).
- For `BigDecimal`, prefer `compareTo` ([idiom 1](#bigdecimal-for-money)).
- **Read more:** [`Object.equals`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/Object.html#equals(java.lang.Object))

<a id="immutable-collections"></a>
## 5. `List.of`, `Set.of`, `Map.of` are immutable

**Rule:** the `of` factories (and a stream's `.toList()`) return collections you can't change. Use them for fixed data; copy into `new ArrayList<>(…)` when you need to add.

```java
private static final Set<String> CODES = Set.of("GBP", "EUR", "USD"); // SupportedCurrencies
List.of("GBP").add("EUR");                 // throws UnsupportedOperationException
List<String> codes = new ArrayList<>(List.of("GBP")); // a changeable copy
codes.add("EUR");                          // fine
```

- **Where:** `SupportedCurrencies` (Day 3) and `Map.of(...)` in `SecurityConfig.passwordEncoder` (Day 17). They also refuse `null` elements.
- **Read more:** [Unmodifiable lists](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/List.html#unmodifiable)

<a id="streams"></a>
## 6. Streams: `filter`, `map`, `toList`

**Rule:** to turn one list into another, `stream()`, then `filter` (keep some), `map` (convert each), and `toList()` (Java 16+) to collect.

```java
// The 2022 service (Day 6), Java 11 style
return accountService.findAll().stream()
        .map(AccountResponse::from)              // Account → AccountResponse
        .collect(Collectors.toList());
// Java 16+: .toList() (unmodifiable). With a filter:
accounts.stream().filter(a -> a.getCurrency().equals("GBP")).map(AccountResponse::from).toList();
```

- **Where:** the legacy `AccountController.getAll` (Days 6–13). The modern controller (Day 3 onwards) uses a plain `for` loop for the same job: both are fine, pick the clearer one.
- `AccountResponse::from` is a method reference: shorthand for `a -> AccountResponse.from(a)`.
- **Read more:** [`java.util.stream`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/package-summary.html)

<a id="java-time"></a>
## 7. `java.time`: `LocalDate`, `Instant`; never `java.util.Date`

**Rule:** a calendar date with no time or zone is a `LocalDate`; a moment in time (for logs, audit, "created at") is an `Instant` (UTC). Don't use `java.util.Date`, `Calendar` or `SimpleDateFormat`: mutable, confusing and not thread-safe.

```java
@NotNull LocalDate onboardingDate            // Onboarding Tracker: JSON "2026-11-10" ↔ LocalDate
LocalDate.parse("2026-11-10")                // ISO yyyy-MM-dd; "10-11-2026" throws → 400 in the API
Instant now = Instant.now();                 // 2026-11-11T09:30:00Z: a moment, for timestamps
```

- **Where:** `Employee.onboardingDate` in the Onboarding Tracker (Day 0). The Accounts API stores no dates yet; its audit lines (Day 18) get their timestamp from the logger.
- **Read more:** [`java.time`](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/package-summary.html)

<a id="exceptions"></a>
## 8. Checked vs unchecked exceptions

**Rule:** a **checked** exception (`Exception`, `IOException`) must be caught or declared with `throws`; an **unchecked** one (`RuntimeException` and its subclasses) needn't be. Our domain exceptions are unchecked, and `ApiExceptionHandler` turns each into a problem detail.

```java
public class InsufficientFundsException extends RuntimeException { … }    // unchecked: no throws needed

@Bean // HttpSecurity.build() declares a checked Exception, so we pass it on
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { … }
```

- **Where:** `AccountNotFoundException` (Day 1), `UnsupportedCurrencyException` (Day 3), `InsufficientFundsException` (Day 19); `throws Exception` in `SecurityConfig` (Day 14) and on MockMvc tests (Day 5).
- **It matters on Day 19:** `@Transactional` rolls back on unchecked exceptions (and `Error`s) by default, **not** on checked ones. Another reason our exceptions extend `RuntimeException`.
- Never swallow an exception with an empty `catch`: the transfer "succeeds" half-done.
- **Read more:** [Oracle: unchecked exceptions, the controversy](https://docs.oracle.com/javase/tutorial/essential/exceptions/runtime.html), [Spring: rollback rules](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/annotations.html)

<a id="constructor-injection"></a>
## 9. `final` and constructor injection

**Rule:** a class gets what it needs through its **constructor**, stored in `private final` fields. With one constructor, Spring uses it automatically: no `@Autowired`.

```java
// Day 3's starter: field injection, and the controller reaching past the service to the repository
@Autowired private AccountService accountService;
@Autowired private AccountRepository accountRepository;
// The refactor
private final AccountService accountService;
public AccountController(AccountService accountService) { this.accountService = accountService; }
```

- **Why:** `final` means it's set exactly once and never `null` afterwards; the constructor shows every dependency at a glance; a unit test can just call `new AccountService(mockRepository)` (Day 4, or `@InjectMocks`).
- `final` on a local variable or field = "can't be reassigned". It doesn't make the *object* immutable.
- **Where:** every controller and service from Day 3; the Day 3 refactor removes the field injection.
- **Read more:** [Spring: constructor-based injection](https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html#beans-constructor-injection)
