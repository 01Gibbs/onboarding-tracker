# Day 00 afternoon — Build an Onboarding Tracker REST API

> **In plain English:** We build a small web service from scratch that keeps a list of new joiners. You send it details, it stores them and gives them back. It's the shape of almost every service the team looks after.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Controller, Repository and H2** (see the [Accounts API diagram](../reference/accounts-api.md)).

There's no starter today (learners generated their own project). The whole solution, from the course root:

```bash
ls day-00-onboarding-tracker/solution/src/main/java/com/example/onboarding
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

1. **`Employee.java`**: An `@Entity` class (not a record: JPA needs a no-argument constructor and setters/reflection). `LocalDate onboardingDate`: dates use `java.time`, never `java.util.Date`.
2. **`CreateEmployeeRequest.java`**: A **record** DTO with `@NotBlank` / `@NotNull`. The client never sends `id`.
3. **`EmployeeController.java`**: `@Valid @RequestBody` triggers validation → 400. `ResponseEntity.created(uri).body(saved)` gives 201 + `Location`. `GET` returns `findAll()`: an empty list serialises to `[]`.
4. **Dates**: `"10-11-2026"` never reaches our code: Jackson can't parse it into a `LocalDate`, so Spring answers 400 itself. Same for `2026-02-30`.
5. **`EmployeeApiTest.java`**: The brief's contract as 8 tests, through the whole app with an in-memory H2 (`@SpringBootTest` + MockMvc). Tomorrow's work is checked the same way.
6. **`bruno/`**: The brief's section 6 as 7 requests with assertions: run the collection, read ✅/❌.

## Points to land

- If AI wrote parts of your solution, today's question is: could you explain every annotation in it? Walk through `reference/java-idioms.md`: records vs classes, `LocalDate`, `BigDecimal` for money (tomorrow's domain is accounts), `Optional`, `List.of`, `==` vs `.equals`.
- Validation belongs at the edge (`@Valid`), before the database ever sees bad data. Without it, a missing field is a 500 from the database, not a 400.
- 201 + `Location` is how HTTP says "created, and here it is".
- Tomorrow switches domain to an **Accounts API**: same shape (entity, repository, POST/GET, validation), plus money.

## Common mistakes

- Returning 200 from POST, or no body.
- `String onboardingDate` with a regex instead of `LocalDate` (you then have to reject `2026-02-30` yourself).
- Running `./gradlew` from the wrong folder: `no such file or directory: ./gradlew`.

## Questions for the room

- **What happens without `@Valid`?**
  <details><summary>Answer</summary>The constraints are ignored. A blank name is saved; a missing one fails at the database as a 500.</details>
- **Why is `Employee` a class but the request a record?**
  <details><summary>Answer</summary>JPA needs a mutable class with a no-argument constructor; the request is immutable data.</details>

## In our code (2 minutes)

Prepare before the session: find one entity and its repository in one of our own services. Which ID generation does it use? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

An Accounts API at exactly tonight's level: finish it with `PUT`, `DELETE`, 404s and one consistent error format.
