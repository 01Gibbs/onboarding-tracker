# Day 02 afternoon — Code Review: Six Bugs

> **In plain English:** Someone rushed a change and it broke six things. We follow each symptom back to its cause, the way a developer investigates a bug report from users.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Controller and Service** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-02-six-bugs/starter day-02-six-bugs/solution
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

1. **Bug 1: `c == "GBP"`**: `==` compares object identity. The JSON's `"GBP"` is a different object. Fixed for good by `SupportedCurrencies` (a `Set.of(...)`).
2. **Bug 2: `double openingBalance` + `new BigDecimal(double)`**: Copies binary rounding error exactly. The DTO field becomes `BigDecimal`.
3. **Bug 3: missing `@Valid`**: The constraint annotations do nothing without it.
4. **Bug 4: `return account`**: Back to `ResponseEntity.created(uri).body(...)`.
5. **Bug 5: `findById(id).get()`**: `NoSuchElementException` → 500. `orElseThrow` → our 404.
6. **Bug 6: `@GetMapping("/delete/{id}")`**: GET must be safe: crawlers and link previews send GETs.

## Points to land

- Follow the symptom: status code → log → stack trace. Top line = *what*; first `com.example` line = *where*.
- Fix one bug at a time and re-test; that's how you know which change fixed what.

## Common mistakes

- Fixing bug 1 with `.equals` chains and stopping there. It works, but the rule belongs in one named place.
- Mapping `IllegalArgumentException` to 400 globally: it would hide real bugs as client errors.

## Questions for the room

- **Which of the six would a test have caught most cheaply?**
  <details><summary>Answer</summary>Bugs 1, 2 and 5 with a service unit test; 3, 4 and 6 with a controller slice test. That's Days 4 and 5.</details>

## In our code (2 minutes)

Prepare before the session: find a real bug-fix pull request in our services: find the line that was wrong and the test that now guards it. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

The same pull request now works, but its design has drifted. Tomorrow: refactor it without changing what a client sees.
