# Day 16 afternoon — Find and Fix an Injection

> **In plain English:** We find and fix code that let a user's input be run as part of a database command, one of the oldest and most damaging security mistakes.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`AccountRepository`** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-16-injection/starter day-16-injection/solution
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

1. **The vulnerable `findMine`**: JPQL built with `+` from the `currency` parameter.
2. **`AccountSearchTest`**: `GBP' OR '1'='1` turns the `WHERE` into `... AND currency = 'GBP' OR '1'='1'`: every account, Grace's included. It bypasses Day 15.
3. **The fix**: `List<Account> findByUsernameAndCurrency(String username, String currency)`: bound parameters. `EntityManager` removed.

## Points to land

- Injection = untrusted input reaching an interpreter. An ORM doesn't protect you if you build the string yourself (OWASP's own HQL example).
- Fix with bound parameters (query methods, `:named`, `setParameter`), never escaping. Validation is supplementary.
- Column names can't be bound: use an allow-list.

## Common mistakes

- Validating `currency` with a regex and calling it fixed.
- Escaping quotes by hand.

## Questions for the room

- **Where else does untrusted input reach an interpreter in a service?**
  <details><summary>Answer</summary>Logs (tomorrow), shell commands, LDAP, templates, JSON paths.</details>

## In our code (2 minutes)

Prepare before the session: find any `createQuery(`/`createNativeQuery(` near a `+` in our code. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Plain-text `{noop}` passwords are still in the code. Tomorrow: store them as salted, slow hashes.
