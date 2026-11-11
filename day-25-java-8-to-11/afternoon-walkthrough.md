# Day 25 afternoon — From Java 8 to 11

> **In plain English:** Some of the team's services still run on Java 8, a version from 2014 that's long past its prime. Today we move one of them to Java 11. The move breaks the service, because Java 11 removed a built-in piece it relied on for producing XML files, so we add that piece back as a separate library and prove everything works again.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

What changes today: **the JDK under a small export service, and its XML library**.

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-25-java-8-to-11/starter day-25-java-8-to-11/solution
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

1. **The starter on Java 8**: `./gradlew -q javaToolchains` (on an Apple Silicon Mac, an x64 Temurin 8 "Auto-provisioned by Gradle"), `./gradlew test` (1 red: "the tests run on Java 1.8"), and `/actuator/info` showing `1.8.0_504`.
2. **`grep -r javax.xml.bind src`**: `Account`, `AccountsXml`, `XmlExporter`, `AccountController`. On Java 8 these classes come from the JDK itself: nothing in `build.gradle.kts` provides them.
3. **Flip the toolchain to 11**: `./gradlew compileJava` → `error: package javax.xml.bind does not exist`. Open JEP 320's list of removed modules.
4. **Half a fix: only `jakarta.xml.bind-api`**: It compiles, then 5 of 6 tests fail with `JAXBException: Implementation of JAXB-API has not been found on module path or classpath`. The context test fails too, because `XmlExporter` builds its `JAXBContext` in its constructor.
5. **The whole fix**: Add `runtimeOnly("org.glassfish.jaxb:jaxb-runtime")`. `dependencyInsight` shows 2.3.3 and 2.3.9, both "selected by rule" (Boot's managed versions). 6/6 green, the log says `using Java 11.0.33`, Bruno 3/3.
6. **Why there's no JPA here**: In a Boot 2.7 app with `spring-boot-starter-data-jpa`, Hibernate 5.6 already brings `jaxb-api` and `jaxb-runtime` transitively, so the same upgrade would *not* break. Worth knowing which of our services rely on that by accident.

## Points to land

- JDK upgrades can remove things: Java 11 removed JAXB, JAX-WS, JAF, Common Annotations (`javax.annotation`) and CORBA (JEP 320). The fix is ordinary dependencies, at the framework's managed versions.
- An API to compile against, an implementation to run: two dependencies in two different configurations.
- `jakarta.xml.bind-api` **2.3.x** keeps the `javax.xml.bind` package; **3.x and later** rename it to `jakarta.xml.bind`, which belongs with Boot 3 (Day 11), not with this upgrade.
- Same pattern as Day 13: inspect, check support from primary sources, change one thing, prove it.

## Common mistakes

- Adding only the API: it compiles, then fails at run time with `Implementation of JAXB-API has not been found`.
- Picking the newest JAXB from Maven Central (`jakarta.xml.bind-api` 4.x): every `javax.xml.bind` import breaks, because the package was renamed.
- Writing versions (`:2.3.3`) into `build.gradle.kts`: works today, but fights Boot's dependency management on the next upgrade.
- Changing `JAVA_HOME` instead of the toolchain: the build still compiles and tests on Java 8 (Day 13).

## Questions for the room

- **Why does the red test use `System.getProperty("java.specification.version")` and not `Runtime.version().feature()`?**
  <details><summary>Answer</summary>The starter compiles on Java 8, where `Runtime.version()` doesn't exist (it arrived in Java 9). The property exists on every version: `1.8` before, `11` after (JEP 223).</details>
- **A teammate says "we've moved loads of Boot 2.7 services to Java 11 and JAXB never came up". How can that be?**
  <details><summary>Answer</summary>Something else already brought JAXB in transitively, most often Hibernate 5.x via `spring-boot-starter-data-jpa`. Check with `dependencyInsight --dependency jaxb-runtime` before relying on it.</details>

## In our code (2 minutes)

Prepare before the session: find a Java 8 or 11 service of ours, and search it for `javax.xml.bind`, `javax.xml.ws`, `javax.activation` and `javax.annotation`. For each hit, which dependency supplies that package today, or does only the JDK? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: the organisation's security scanner has sent a report on the Accounts API. Triage it finding by finding: fix it, prove it's a false positive, accept the risk, or mark it a duplicate.
