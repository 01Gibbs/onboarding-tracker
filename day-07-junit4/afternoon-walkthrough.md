# Day 07 afternoon — Read, Write and Migrate JUnit 4 Tests

> **In plain English:** Older services often have older tests. We learn to read them, add one, and move them to the current testing tool so they keep running after upgrades.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **the tests** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-07-junit4/starter day-07-junit4/solution
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

1. **JUnit 4 `AccountServiceTest`**: `@RunWith(MockitoJUnitRunner.class)`, `@Before`, `@Test(expected = …)`, `Assert.assertEquals(expected, actual)`: expected first.
2. **The three new tests**: `Assert.assertThrows` exists from JUnit 4.13.
3. **Migration**: `@RunWith` → `@ExtendWith` (or nothing with Spring Boot 2.2+ `@SpringBootTest`), `@Before` → `@BeforeEach`, `expected` → `assertThatThrownBy`, `Assert` → AssertJ.
4. **`build.gradle.kts`**: `junit-vintage-engine` removed. Count before = count after = 9.

## Points to land

- The JUnit Platform runs engines: Jupiter (5) and Vintage (4) side by side.
- The trap: `junit:junit` still on the classpath without Vintage compiles, goes green, and silently runs only the JUnit 5 tests.
- OpenRewrite has recipes for this (`SpringBoot2JUnit4to5Migration`); Boot 3 upgrade recipes include it.

## Common mistakes

- `assertEquals(actual, expected)`: passes or fails the same, but the failure message is backwards.
- Trusting a green build without checking the test count.

## Questions for the room

- **Why migrate tests before the Boot 3 upgrade, not during?**
  <details><summary>Answer</summary>One change at a time: if the upgrade breaks something, you want a test suite you trust.</details>

## In our code (2 minutes)

Prepare before the session: find any `org.junit.Test` import left in our services. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

What does this service actually ship? Lockfile, scan, trace.
