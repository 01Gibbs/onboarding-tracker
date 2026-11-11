# Day 24 afternoon — Maven for Gradle Users

> **In plain English:** Many services are built with a tool called Maven rather than the Gradle we've used so far. Today we take the API we know from Day 5, now built with Maven, and do one common job with it: a ticket says one library must move to a newer patch version, so we make the change, prove it took effect, and keep everything working.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the test output (`target/surefire-reports/`), and a terminal in `solution/`.

Where today's change sits in the app: **the build (`pom.xml`): Maven instead of Gradle** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-24-maven/starter day-24-maven/solution
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

1. **`pom.xml` next to Day 5's `build.gradle.kts`**: `<parent>` does the dependency-management plugin's job; `<java.version>` is the toolchain line, but with no download (`./mvnw -v` shows `JAVA_HOME`'s JDK); each `<dependency>` has a `<scope>` and no version.
2. **`./mvnw test` on the starter**: `Tests run: 16, Failures: 1`: "jackson-databind on the classpath is 3.1.7; the ticket needs 3.1.8 or later". Open `target/surefire-reports/` and compare it with `build/test-results/`.
3. **`./mvnw dependency:tree -Dincludes=tools.jackson.core`**: The chain `spring-boot-starter-webmvc` → `spring-boot-starter-jackson` → `spring-boot-jackson` → `jackson-databind:3.1.7` → `jackson-core:3.1.7`.
4. **The one-line fix**: `<jackson-bom.version>3.1.8</jackson-bom.version>` in `<properties>`. Re-run the tree: both modules are at 3.1.8.
5. **`help:effective-pom`**: Search for `jackson-databind`: `tools.jackson.core` at 3.1.8 (ours) and `com.fasterxml.jackson.core` at 2.21.7 (Jackson 2, from `jackson-2-bom.version`, untouched). `jackson-annotations` stays on 2.21 by design in Jackson 3.
6. **`JacksonVersionTest`**: It asks the jar for its version at run time, which is why a comment or a wrong property name can't turn it green. Then `./mvnw spring-boot:run`: the Day 5 Bruno collection still passes.

## Points to land

- Maven and Gradle do the same jobs with different words: the translation table in the brief is the takeaway. Everything from Days 8–9 (trees, transitive dependencies, managed versions) carries over.
- Override the **BOM property** Boot reads, not the single artifact: that keeps every module of the library on the same version (`jackson-core` moved too).
- Property overrides work because of `spring-boot-starter-parent`. A service that imports `spring-boot-dependencies` as a BOM instead must import the library's BOM *before* Boot's.
- Prove it from the resolved build (`dependency:tree`, `effective-pom`, a test), never from what the file says.

## Common mistakes

- Adding `<version>3.1.8</version>` to a new `jackson-databind` dependency: the test goes green, but `jackson-core` stays at 3.1.7 (mixed module versions), and the next Boot upgrade keeps the old pin.
- Guessing the property name (`<jackson.version>`, `<jackson-databind.version>`): Maven silently ignores a property nothing reads. No error, no change.
- Searching the tree for `com.fasterxml.jackson.core`: that's Jackson 2's group id. Boot 4 uses Jackson 3 (`tools.jackson.core`).
- Running Maven on the wrong JDK: on Java 21, `./mvnw compile` fails with "release version 25 not supported". Maven doesn't download JDKs; check `./mvnw -v`.

## Questions for the room

- **Why did changing one property move two jars?**
  <details><summary>Answer</summary>`jackson-databind` and `jackson-core` both get their version from Jackson's BOM, which Boot imports at `${jackson-bom.version}`. One property, every module.</details>
- **A service has no `<parent>` and imports `spring-boot-dependencies` in `<dependencyManagement>`. Your property override does nothing. Why, and what do you do?**
  <details><summary>Answer</summary>An imported BOM's properties can't be overridden from the importing POM. Import `tools.jackson:jackson-bom:3.1.8` in `<dependencyManagement>` *before* `spring-boot-dependencies` (the first declaration wins), as Boot's "Using Spring Boot without the Parent POM" shows.</details>

## In our code (2 minutes)

Prepare before the session: find a service of ours built with Maven: does it use `spring-boot-starter-parent` or import the BOM? List every `<…version>` property in its `<properties>` and, for each, say whether Boot actually reads it. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: a service that never left Java 8. Moving it to Java 11 breaks the build, because part of the JDK it relied on was removed, and you'll put it back as an ordinary dependency.
