# Day 06 afternoon — Get a 2022 Service Running, and Audit It

> **In plain English:** We pick up an older service written in 2022 and get it running on a modern laptop. Then we write down what's old about it: the starting point for every upgrade.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **the whole (older) app** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-06-a-legacy-service/starter day-06-a-legacy-service/solution
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

1. **`gradle/wrapper/gradle-wrapper.properties`**: Gradle 8.14.6: Boot 2.7's plugin breaks on Gradle 9.
2. **`gradle/gradle-daemon-jvm.properties`**: Gradle itself runs on JDK 17 (8.14 can't run on 25). The error without it is just `What went wrong: 25.0.x`.
3. **`build.gradle.kts` toolchain + `settings.gradle.kts` foojay**: Code compiles on JDK 11; missing JDKs are downloaded.
4. **`SecurityConfig.java`**: `WebSecurityConfigurerAdapter`, `antMatchers`, `.and()` chains, `permitAll()` vs `authenticated()`, `httpBasic()`.
5. **`solution/AUDIT.md`**: Compare markers: `javax.*`, getter/setter DTOs, JUnit 4, `@MockBean`, the adapter, the `h2.version` pin.

## Points to land

- Two JDKs: the daemon runs Gradle, the toolchain compiles your code. Old projects often need both to be older than your laptop's.
- Boot 2.7 open-source support ended 2023-06-30: security fixes stopped.
- An audit is evidence for a decision, not a to-do list.

## Common mistakes

- Installing JDK 11 globally or changing `JAVA_HOME` instead of letting the toolchain handle it.
- Bumping the wrapper to Gradle 9 to "fix" the daemon error.

## Questions for the room

- **What in the 'risky' list worries you most, and why?**
  <details><summary>Answer</summary>Usually the actuator `permitAll()` and the H2 console. Those are Day 10. The `h2.version` pin is Days 8–9.</details>

## In our code (2 minutes)

Prepare before the session: find one of our services' `build.gradle(.kts)`: which Java and Boot versions, and are they still supported? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

This service's tests are half JUnit 4. Read them, add to them, then migrate them.
