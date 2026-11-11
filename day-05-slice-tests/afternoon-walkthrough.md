# Day 05 afternoon — Testing the Web Layer and the Repository

> **In plain English:** We test bigger pieces: the part that answers web requests and the part that talks to the database. Each test checks one layer on its own.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **Controller (web slice) and Repository (JPA slice)** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-05-slice-tests/starter day-05-slice-tests/solution
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

1. **`AccountControllerTest`**: `@WebMvcTest(AccountController.class)` + `MockMvc` + `@MockitoBean AccountService`. Status, `jsonPath`, and `verifyNoInteractions` after a 400.
2. **404 test**: Asserts `$.title == "Account not found"`: proves `ApiExceptionHandler` is part of the slice.
3. **`AccountRepositoryTest`**: `@DataJpaTest`: fresh in-memory H2, each test rolled back.
4. **The pyramid**: Plain → Mockito → `@DataJpaTest` / `@WebMvcTest` → `@SpringBootTest`. Start at the bottom.

## Points to land

- `@MockitoBean` puts a mock *into Spring*; `@Mock` is Mockito only. `@MockBean` was removed in Boot 4 (you'll meet it on Day 6).
- Repository tests earn their keep for custom queries, not for `findById`.
- 15 tests now. Every one runs in seconds.

## Common mistakes

- Asserting only status codes: a 404 from a typo in the URL looks the same as our handler's 404.
- Expecting `@DataJpaTest` to see the app's `./data` file.

## Questions for the room

- **Which test for: a `PUT` with a blank owner is 400? `findByCurrency` filters? `delete` throws for a missing id?**
  <details><summary>Answer</summary>`@WebMvcTest`; `@DataJpaTest`; Mockito unit test.</details>

## In our code (2 minutes)

Prepare before the session: find the test report of one of our services: how many tests of each kind? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

A different service: the same API written in 2022 on Java 11 and Boot 2.7. Get it running and audit it.
