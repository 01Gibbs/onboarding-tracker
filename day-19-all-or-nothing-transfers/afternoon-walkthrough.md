# Day 19 afternoon — All or Nothing: Safe Transfers

> **In plain English:** A transfer moves money out of one account and into another. We make sure it happens completely or not at all: never half.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`AccountService`** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-19-all-or-nothing-transfers/starter day-19-all-or-nothing-transfers/solution
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

1. **The colleague's `transfer`**: Withdraw, save, *then* load the destination. A missing destination → 404, but £40 has gone.
2. **`TransferTest`**: `@SpringBootTest` with an in-memory DB, **not** `@DataJpaTest`, whose per-test transaction would hide the bug.
3. **The fix**: `@Transactional` (rollback on `RuntimeException`) + load and check both accounts before changing either.

## Points to land

- Failing closed: a failure must leave nothing half-done (OWASP A10:2025, scenario #3).
- `@Transactional` is the safety net; check-then-change is the habit. Either alone isn't the whole story.
- `@Transactional` only applies through the Spring proxy: calls from another bean, not from inside the same class.

## Common mistakes

- Catching the exception and "refunding" by hand.
- Testing with `@DataJpaTest` and seeing green.

## Questions for the room

- **Two transfers out of the same account at once both pass the balance check. What now?**
  <details><summary>Answer</summary>Optimistic locking (`@Version`) or a database constraint.</details>
- **Checked exceptions and rollback?**
  <details><summary>Answer</summary>By default Spring rolls back on unchecked exceptions only; use `rollbackFor` otherwise.</details>

## In our code (2 minutes)

Prepare before the session: find a method in our code that changes two things. Is it transactional? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Day 19's transaction protects one request. Tomorrow: two requests change the same account at the same moment, and one withdrawal vanishes.
