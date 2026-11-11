# Day 21 afternoon — Idempotent Payments

> **In plain English:** If a payment request is sent twice (say, the network dropped and the app retried), the customer must only be charged once. We make that guaranteed.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Controller, `AccountService`, new `IdempotencyRecord` table** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-21-idempotent-payments/starter day-21-idempotent-payments/solution
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

1. **`model/IdempotencyRecord.java`**: Unique on (username, key). The column is `idempotency_key` because `key` is reserved in SQL. Stores the request fingerprint and the status answered.
2. **`service/AccountService.transfer(…, idempotencyKey)`**: Look up the record: different fingerprint → 422; same → replay the stored status; otherwise transfer and save the record, in **one** `@Transactional`.
3. **The fingerprint**: `from=1 to=3 amount=50` with `stripTrailingZeros()`, so `50` and `50.00` are the same request.
4. **`controller/AccountController.transfer`**: `@RequestHeader(required = false)` plus our own check, so the 400 carries our title; `ResponseEntity.status(...)` replays the stored answer.
5. **Failed transfers**: Not stored: the rollback takes the key with it, so the client can retry a 404 or insufficient funds.

## Points to land

- Idempotency is a contract between client and server: the client owns the key, the server remembers the outcome.
- Saving the key in the same transaction as the payment is the whole trick.
- Keys are per user: two customers may pick the same key.

## Common mistakes

- Saving the key in a separate transaction (key without payment, or payment without key).
- Fingerprinting the raw body text.
- Leaving `@RequestHeader` required: Spring's generic 400 has no title.

## Questions for the room

- **Two first requests with the same key arrive together. What happens?**
  <details><summary>Answer</summary>Both pass the lookup; the unique constraint rejects the second with `DataIntegrityViolationException` (a 500 today). Map it to 409.</details>

## In our code (2 minutes)

Prepare before the session: find how our payment APIs (or a provider we call) handle retries. Is there an idempotency key? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Ada can still send pounds into a dollar account. Tomorrow: money that knows its currency.
