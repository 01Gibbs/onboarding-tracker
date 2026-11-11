# Day 27 afternoon — Zero-Day Drill

> **In plain English:** A serious security hole has been announced in a logging library that thousands of programs use. We have hours, not weeks, to find out which of our services contain it, update them, prove the update worked and report back. Today is a practice run of that emergency.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, each subproject's HTML test report (`<subproject>/build/reports/tests/test/index.html`), and a terminal in `solution/`.

What changes today: **the libraries under every service, across the estate**.

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-27-zero-day-drill/starter day-27-zero-day-drill/solution
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

1. **`settings.gradle.kts` and `./gradlew projects`**: One build, five projects; task paths like `:services:payments:test`.
2. **The find**: The `dependencyInsight` loop over the three services. Statements' path is two hops (`statements → reporting → audit → log4j-core`): nobody on that team ever typed "log4j". Notifications ships `log4j-api` + `log4j-to-slf4j` and no `log4j-core`, and Apache says only `log4j-core` is affected. (Spring Boot's default set-up looks exactly like this.)
3. **`gradle/libs.versions.toml` and `libs/audit/build.gradle.kts`**: The catalog said 2.14.1, but `audit` hard-coded its own copy. Bump only the catalog: the services still ship 2.14.1. Bump only `audit`'s line: the services are patched, but the catalog still says 2.14.1, and `log4jCoreIsTheVersionInTheCatalog` fails. The fix: `audit` uses `libs.log4j.core`, and the catalog says `2.26.1`.
4. **`PaymentsLog4jVersionTest`**: `Class.forName` (the library is runtime-only), then `getPackage().getImplementationVersion()` from the jar's manifest. `log4jCoreIsPatched` compares it with a floor of 2.17.1; `log4jCoreIsTheVersionInTheCatalog` compares it with the catalog's `log4j` entry. It checks what's loaded, not what a file says.
5. **`./gradlew test` at the top**: One command runs every project's tests; `--continue` shows every red service, not only the first.
6. **`INCIDENT.md` (solution)**: The table, the commands, the fix, the evidence, and "not affected, and here's why" for notifications.

## Points to land

- In an incident the first question is **"where is it?"**, and the answer is almost always transitive. Look at what ships (`runtimeClasspath`), per service.
- Pick the version from primary sources, and take the fix for the whole family of CVEs (2.17.1 or later), not the first patch (2.15.0). Apache's page and NVD disagree slightly on CVE-2021-44832: take the stricter answer. Then use a current release.
- One version, one place: a catalog or a BOM makes the next emergency a one-line change. Hard-coded versions in shared libraries are where fixes go missing.
- "Not affected" is a finding too, and needs evidence as much as "fixed" does.

## Common mistakes

- Running `dependencyInsight` on `compileClasspath`, or on the root project, and concluding "not there".
- Choosing 2.15.0 or 2.16.0 because it fixes "the" CVE.
- Bumping only `libs/audit`'s hard-coded line: the services are patched, but the estate now has two Log4j versions and the catalog still says 2.14.1.
- Bumping only the catalog and declaring victory without running the tests.
- Calling notifications affected because it has "log4j" in its dependencies.

## Questions for the room

- **The fix is merged. Is the incident closed?**
  <details><summary>Answer</summary>No: not until the rebuilt services are deployed everywhere they run, and the other teams using `libs/audit` have taken the new version.</details>
- **What would have made this faster?**
  <details><summary>Answer</summary>A lockfile or SBOM per service somewhere searchable (then it's a query, not a build), one version source per library, and an SCA scanner that alerts on new advisories.</details>

## In our code (2 minutes)

Prepare before the session: find one of our services, and answer "do we ship log4j-core, and which version?" in under five minutes. Where are its lockfile, SBOM or SCA results? Which shared libraries do several of our services use, and where are their versions set? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: the libraries are patched, but the service ships inside a container image with its own Java runtime and operating system. Is the base image patched too?
