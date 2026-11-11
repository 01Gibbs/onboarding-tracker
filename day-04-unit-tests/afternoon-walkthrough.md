# Day 04 afternoon — Unit Tests with JUnit 5, AssertJ and Mockito

> **In plain English:** We write automatic checks (tests) that prove small pieces of the service work. They run in seconds and catch mistakes before anyone else sees them.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **Service (tested alone)** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-04-unit-tests/starter day-04-unit-tests/solution
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

1. **`SupportedCurrenciesTest`**: Plain JUnit + AssertJ: no Spring, milliseconds. Names describe behaviour.
2. **`AccountServiceTest`**: `@ExtendWith(MockitoExtension.class)`, `@Mock AccountRepository`, `@InjectMocks AccountService`. Arrange (`when`) / Act / Assert.
3. **`createSavesTheNewAccount`**: `thenAnswer(call -> call.getArgument(0))`: a mock's `save` returns `null` unless scripted.
4. **`createRejectsAnUnsupportedCurrency`**: `assertThatThrownBy` + `verify(repo, never()).save(any())`: proving something did *not* happen.

## Points to land

- `isEqualByComparingTo("10.00")` for money: `isEqualTo` fails on `10.0` vs `10.00` (scale).
- Test behaviour, not implementation: would this test survive a refactor that keeps behaviour?
- The HTML report says *why* a test failed; the terminal often only says *that* it did.

## Common mistakes

- Using `@SpringBootTest` for a unit test: slow, and tests the wiring instead of the logic.
- Over-verifying (`verify` on every call): tests then break on harmless refactors.
- Not seeing a test fail first. Break the code on purpose once.

## Questions for the room

- **When is a mock the wrong tool?**
  <details><summary>Answer</summary>When the thing you want to check *is* the dependency's behaviour, e.g. a query. That needs a real database: Day 5.</details>

## In our code (2 minutes)

Prepare before the session: find a Mockito test in our services. Does it test behaviour or implementation? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tests that start part of Spring: the web layer and JPA.
