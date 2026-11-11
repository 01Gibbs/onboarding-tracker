# Day 21 hints — Idempotent Payments

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Remember completed payments

1. You need to look up "have I already done this payment for this user?"
2. A small entity keyed by (username, key) with the request's fingerprint and the status you answered. `key` is a reserved SQL word: name the column something else.
3. In `transfer`: find the record; if it exists, compare fingerprints (different → 422, same → return the stored status); otherwise transfer and save the record, all inside the same `@Transactional` method.

## Same request, different spelling

1. Is `50` the same amount as `50.00`?
2. Build the fingerprint from the meaning (from, to, amount), not the raw body text.
3. `"from=" + from + " to=" + to + " amount=" + amount.stripTrailingZeros().toPlainString()`.

## 400 with our title

1. If Spring rejects a missing header itself, the title isn't ours.
2. `@RequestHeader(value = "Idempotency-Key", required = false)`.
3. Check it in the controller and throw your own `IdempotencyKeyRequiredException` → 400 `Idempotency key required`.
