# Day 20 afternoon — Two Transfers at Once

> **In plain English:** When two transfers touch the same account at the same moment, one can silently overwrite the other. We make the service notice and refuse.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`Account` entity, `ApiExceptionHandler`** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-20-two-transfers-at-once/starter day-20-two-transfers-at-once/solution
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

1. **`model/Account.java`**: `@Version private Long version;`. Show the SQL Hibernate now writes: `UPDATE … SET …, version = ? WHERE id = ? AND version = ?`.
2. **`ConcurrentTransferTest`**: Walk its five numbered steps: two `TransactionTemplate`s (the inner one `REQUIRES_NEW`) script the interleaving. No threads, no sleeps, so it's deterministic.
3. **`exception/ApiExceptionHandler.java`**: `@ExceptionHandler(OptimisticLockingFailureException.class)` → 409 `Concurrent update`. Spring has already translated Hibernate's `StaleObjectStateException`.
4. **`AccountControllerTest.aConcurrentUpdateIs409`**: The learners' test: the mock throws `ObjectOptimisticLockingFailureException`, and the answer is 409 + "try again".

## Points to land

- A lost update isn't a transaction bug: each transaction is fine on its own. It's two of them racing.
- Optimistic locking assumes conflicts are rare: detect them at commit and let the client retry. Pessimistic locking (`@Lock`) blocks instead.
- The failure happens at flush/commit, not at `save()`.
- `synchronized` doesn't help: production runs several instances of the app.

## Common mistakes

- Forgetting to delete `data/`: the new NOT NULL `version` column can't be added, and every transfer is a 500.
- Catching Hibernate's own exception types: Spring has translated them.
- Mapping the conflict to 500 or 400: 409 + "try again" tells the client what to do.

## Questions for the room

- **Should the server retry a 409 itself?**
  <details><summary>Answer</summary>Sometimes, for safe operations. For payments, only if the retry can't pay twice, which is exactly tomorrow's problem.</details>

## In our code (2 minutes)

Prepare before the session: find an entity in our services with `@Version`, or a place where two requests could update the same row. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

The client sees a 409, or a timeout, and retries. Tomorrow: make sure a retried payment never pays twice.
