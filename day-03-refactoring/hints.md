# Day 03 hints — Refactor Without Changing Behaviour

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Constructor injection

1. A `final` field must be set by a constructor.
2. Your IDE can convert a field to a constructor parameter (IntelliJ: Alt+Enter on the field).
3. `private final AccountService accountService;` + a constructor taking it. Spring passes the bean in; remove `@Autowired`.

## Unsupported currency → 400

1. The 500 comes from an `IllegalArgumentException`. Day 1 showed how to map an exception once.
2. Create a specific exception for this case rather than mapping every `IllegalArgumentException`.
3. `UnsupportedCurrencyException` thrown by the service + an `@ExceptionHandler` returning a 400 `ProblemDetail` titled `Unsupported currency`.

## Don't return the entity

1. What would happen to the public JSON if someone added an internal column to `Account`?
2. A record can carry exactly the fields the API promises.
3. `record AccountResponse(Long id, String owner, String currency, BigDecimal balance)` with a `static from(Account)`; map in the controller.
