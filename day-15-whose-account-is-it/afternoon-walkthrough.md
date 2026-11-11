# Day 15 afternoon — Whose Account Is It? Fixing an IDOR

> **In plain English:** We fix a common security hole where a logged-in customer could see someone else's account just by changing a number in the address.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`AccountService`** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-15-whose-account-is-it/starter day-15-whose-account-is-it/solution
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

1. **`Account.username`**: `@Column(nullable = false)`. Seeder and `CreateAccountRequest` carry it.
2. **`AccountService.getVisibleTo`**: Customer + not theirs → `AccountNotFoundException` (404, not 403).
3. **`AccountController.getById(..., Authentication caller)`**: Spring passes the logged-in user; `isTeller` checks `ROLE_TELLER`.
4. **Tests**: Three Mockito tests; controller stubs use `@WithMockUser`'s default username `user`.

## Points to land

- URL rules know *who* and *which URL*; only the data knows *whose*. Ownership belongs in the service.
- 404 doesn't confirm the record exists; 403 does.
- Every endpoint that takes an id needs the same question.

## Common mistakes

- Checking ownership in the controller (bypassed by the next caller of the service).
- Trusting a `username` sent in the request instead of the authenticated one.

## Questions for the room

- **Which other endpoints need an ownership check?**
  <details><summary>Answer</summary>Any non-teller endpoint taking an id. Day 19's transfers is one.</details>

## In our code (2 minutes)

Prepare before the session: find an endpoint in our services that takes an id. Where is ownership checked? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

A colleague's search endpoint builds its query with `+`.
