# Day 29 afternoon — The Next Hop: Spring Boot 4 on Java 25

> **In plain English:** The old service now runs on a version of its main framework that no longer gets free security fixes. The team's newer services already run on the next big release of that framework (version 4) and the latest Java. Today we move the old service up to match. We do it one step at a time and write down every error we hit, because the list of errors and fixes is what the next team will need.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **the whole app's platform** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-29-spring-boot-4-and-java-25/starter day-29-spring-boot-4-and-java-25/solution
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

1. **`starter/…/PlatformVersionTest.java`**: The goal state as a test. Plain JUnit, no Spring context, so it compiles and runs on both platforms: `Runtime.version().feature()` and `SpringBootVersion.getVersion()` (which exists in both 3.5 and 4).
2. **`solution/BOOT4-UPGRADE.md`**: Walk the log row by row. The order: wrapper first (the smallest step), then Boot and Java together, then fix what the build reports first.
3. **`build.gradle.kts`, starter vs solution**: Plugin and toolchain lines, `-web` → `-webmvc`, the split test starters, `spring-boot-h2console`, and Day 12's Tomcat override gone.
4. **The three test files**: Only imports changed, plus `@AutoConfigureTestRestTemplate` on `H2ConsoleTest`.
5. **`gradle/`**: The wrapper properties say 9.8.2 and `gradle-daemon-jvm.properties` is gone. Run `./gradlew -version` in both projects.
6. **The `gradle.lockfile` diff**: `com.fasterxml.jackson` → `tools.jackson`, Tomcat 11, Spring Framework 7, Hibernate 7, JUnit 6.

## Points to land

- A major upgrade is a loop: one change, build, read the first error, fix, log. The log is the reusable asset for the next service.
- Most Boot 4 breakage in a small app is *build* breakage (modules, starters, packages). The code under `src/main` didn't change at all.
- Green is not done: two real problems (Tomcat pinned back to 10.1, the dev H2 console gone) produced no failing test. Check what you resolved, and what you used to have.
- Read the vendor's guide, not just a tool's patch: OpenRewrite's 4.0 recipe would have stopped at 4.0.x, whose open-source support ends on 2026-12-31.

## Common mistakes

- Running `./gradlew` from the day folder instead of `starter/`, or editing `PlatformVersionTest` to make it pass.
- Forgetting `--write-locks` after a dependency change, then "fixing" the lock error by deleting `dependencyLocking { }`.
- Switching to `spring-boot-starter-classic` to get green and stopping there; the guide calls it an intermediate step.
- Leaving `extra["tomcat.version"]` in because the build is green.

## Questions for the room

- **Why did `H2ConsoleTest` keep passing after the H2 console disappeared?**
  <details><summary>Answer</summary>It checks that the console is **off** (404) outside `dev`. Without the module the console is off everywhere, so the test can't tell. A test that it's **on** in `dev` would have caught it.</details>
- **Could we have skipped Gradle 9?**
  <details><summary>Answer</summary>For Boot 4.1.2, yes (it supports Gradle 8.14 and later), but not for running Gradle itself on Java 25, which needs Gradle 9.1 or later. Staying on 8.14 means keeping the JDK 17 daemon pin.</details>

## In our code (2 minutes)

Prepare before the session: find a service of ours still on Boot 3.x: which `spring-boot-starter-*` dependencies does it declare, which `org.springframework.boot.test.autoconfigure…` imports do its tests use, and does its build file carry `extra["…version"]` overrides that Boot 4 would make stale? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: today's service was already on Spring Boot; tomorrow's isn't. An older Spring MVC app packaged as a WAR, with XML configuration, moves onto Spring Boot for the first time.
