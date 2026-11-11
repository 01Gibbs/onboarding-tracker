# Day 22 hints — Money Done Right

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## A Money type

1. An amount without its currency is half the information.
2. A `record Money(BigDecimal amount, String currency)` with a compact constructor that checks its inputs.
3. In the compact constructor: `amount = amount.setScale(2, RoundingMode.UNNECESSARY)`. That throws on 10.005 instead of rounding; `add`/`subtract` throw `CurrencyMismatchException` when the currencies differ.

## Half a penny at the edge

1. Bean Validation can limit digits before and after the decimal point.
2. `@Digits(integer = …, fraction = …)` on the request field.
3. `@Digits(integer = 17, fraction = 2)` matches the `precision = 19, scale = 2` column.
