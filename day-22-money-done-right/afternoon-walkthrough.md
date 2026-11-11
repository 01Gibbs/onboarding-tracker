# Day 22 afternoon — Money Done Right

> **In plain English:** Money has rules: different currencies, and a fixed number of decimal places. We stop the service rounding pennies away or mixing currencies.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Model (`Money`), DTOs** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-22-money-done-right/starter day-22-money-done-right/solution
```

## Agenda (25 min)

| Time | What |
|---|---|
| 0–3 | Recap the question. Show of hands: who got it green? Where did people get stuck in the chat today? |
| 3–14 | Solution tour (below), in order |
| 14–19 | Points to land, and common mistakes |
| 19–22 | Questions for the room |
| 22–25 | In our code, then tomorrow's question |

## Solution tour

1. **The starter run**: Sending 10.005 left Ada at 1190.00: the database rounded 1189.995 up. Half a penny from nowhere.
2. **`model/Money.java`**: A record with a compact constructor: `setScale(2, RoundingMode.UNNECESSARY)` means "never round; throw". `add`/`subtract` refuse a different currency.
3. **`model/Account.java`**: `deposit(Money)` / `withdraw(Money)`: a balance can no longer meet a different currency.
4. **`service/AccountService.transfer`**: `new Money(amount, from.getCurrency())`; `to.deposit(money)` throws `CurrencyMismatchException`, and Day 19's `@Transactional` undoes the withdrawal.
5. **`dto/TransferRequest.java`**: `@Digits(integer = 17, fraction = 2)` matches the `precision 19, scale 2` column. The same rule went onto `openingBalance`, the other way money enters.

## Points to land

- An amount without a currency is a bug waiting to happen.
- Round only where a business rule says so (interest, FX, fees), and name the mode (`HALF_EVEN` is the banker's choice). Everywhere else, refuse.
- Validate at the edge *and* in the type: the edge gives a good error, the type makes the mistake impossible.

## Common mistakes

- `setScale(2)` with no mode: it means `UNNECESSARY` and throws at runtime.
- `HALF_UP` in `Money`: it silently accepts 10.005.
- `BigDecimal.equals`: 10.5 ≠ 10.50.

## Questions for the room

- **Why `HALF_EVEN` for a fee, not `HALF_UP`?**
  <details><summary>Answer</summary>Over many transactions `HALF_UP` drifts upwards; `HALF_EVEN` rounds halves to the even digit, so the errors cancel out.</details>

## In our code (2 minutes)

Prepare before the session: find how amounts are represented in one of our services: `BigDecimal`, a money type, or minor units (pence as a `long`)? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

The colleague's next change shows full account numbers to everyone, and writes them to the logs. Tomorrow: mask what you show.
