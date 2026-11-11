# Day 03 afternoon — Refactor Without Changing Behaviour

> **In plain English:** We tidy the code's structure without changing what it does, and prove nothing changed. Tidy code is cheaper and safer to change later.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Controller and Service** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-03-refactoring/starter day-03-refactoring/solution
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

1. **Constructor injection**: `private final` fields set by the constructor; no `@Autowired` fields. Spring passes the beans in.
2. **Layers**: The controller calls only the service; the repository field is gone from the controller.
3. **Names**: `doStuff(r)` → `create(request)`: done with the IDE's Rename, so every usage changes.
4. **`SupportedCurrencies` + `UnsupportedCurrencyException`**: One named home for the rule; a new `@ExceptionHandler` makes it a 400 `Unsupported currency`.
5. **`AccountResponse`**: A record with `from(Account)`: the public contract no longer follows the database model.

## Points to land

- Refactoring = same behaviour. The Bruno collection, run before and after each step, is the proof.
- Constructor injection makes dependencies visible and testable: `new AccountService(fakeRepository)` (tomorrow).
- Response DTOs decouple the database model from the API contract.

## Common mistakes

- Find-and-replace renames instead of the IDE's Rename refactoring.
- Mapping every `IllegalArgumentException` to 400: it would hide real bugs as client errors.

## Questions for the room

- **Why is constructor injection easier to test than `@Autowired` fields?**
  <details><summary>Answer</summary>A test can call `new AccountService(fakeRepository)`: no Spring, no reflection.</details>

## In our code (2 minutes)

Prepare before the session: find a service class in our code with field injection, or one with constructor injection. Which is easier to test? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Write the tests that would have caught these bugs: plain unit tests, then the service with a mocked repository.
