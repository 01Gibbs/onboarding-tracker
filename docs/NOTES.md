# Teaching notes

Working notes and preferences for whoever maintains and extends this course.

## Delivery model (course owner's direction, 2026-11-11)

- **One day = one question and one answer.** Morning: deliver the task in 30 minutes or less (`brief.docx`). During the day: learners work on it, with an ongoing chat for help. Afternoon: a 20–30 minute walkthrough of the solution (`afternoon-walkthrough.md`), which also sets up the next day.
- **Level:** start where the team's "Build an Onboarding Tracker REST API with Spring Boot" brief leaves off. Learners can already generate a Boot project, build an entity and a repository, add POST/GET with validation, and test with Bruno. Don't re-teach that, and don't assume lower knowledge.
- **Briefs look like that workshop brief:** Word `.docx`, sections Overview / Learning objectives (with reference links) / Prerequisites / Getting started / Your task (exact contract tables: paths, fields, status codes, "your work will be checked against them") / Testing with Bruno / If you finish early. **No step-by-step code**: the how belongs in the afternoon walkthrough.
- **No "Ask your AI assistant" sections.**
- **Non-developers follow along too** (analysts, testers, risk and security colleagues). Every day has an **In plain English** summary (`plain` in `course.json`: two or three short sentences, no jargon), shown first in the brief and the afternoon walkthrough. [`reference/start-here.md`](../reference/start-here.md) is their way in. Say "the team", never "fintech".
- **Bruno** is the team's API client. Each day that calls the API ships `bruno/` in the classic `.bru` format (every Bruno version opens it) with `assert` blocks, so a run is a pass/fail check of the brief's contract.
- **Always say where to run:** `cd day-NN-…/starter` and `ls gradlew`. Learners lost time running `./gradlew` from the wrong folder.
- **Upgrade days show the inspection pattern:** `java -version`, `./gradlew -version` (the JVM running Gradle), the toolchain, `./gradlew javaToolchains`, `dependencies` / `dependencyInsight`, `jdeps`.

## Generated files

- **`course.json` is the single source of facts** about each day. Change a folder, title, count or red test there, then run `python3 tools/course/sync.py`, `python3 tools/briefs/walkthroughs.py` and `node tools/briefs/build.js`, and prove it with `python3 tools/course/verify.py`. Never type a count or a day title into prose: use a `{{token}}` (see `facts()` in `tools/course/course.py`).

- `brief.docx` (every day except Day 0, whose brief is the team's original) ← `tools/briefs/briefs.js` + `build.js` (`cd tools/briefs && npm install && node build.js`). US Letter, styled like the Onboarding Tracker brief.
- `afternoon-walkthrough.md` ← `tools/briefs/walkthroughs.json` + `walkthroughs.py`.
- `hints.md` and the recall / definition-of-done / new-terms sections of each brief ← `tools/briefs/extras.js`. Day 0's brief is the team's original `.docx`, copied as-is; its `hints.md` is hand-written.
- Verify a collection without Bruno: `python3 tools/check-bruno.py day-NN-…/bruno [baseUrl]` against a running solution.

## Code gotchas

- **Days 6–13, 25, 28, 30 and Day 29's starter** use Gradle 8.14.6 + `gradle/gradle-daemon-jvm.properties` (daemon on JDK 17) + the foojay resolver, so they build on a JDK-25-only laptop. Never bump the Boot 2.7 projects (Days 6–10, 25, 30) to Gradle 9: Boot 2.7's `bootJar` breaks on it. Day 29's task *is* the move to Gradle 9.8.2, so its solution differs (`course.json` states both versions).
- **Day 24 is Maven** (wrapper 3.9.16, only-script: no jar committed). Maven uses `JAVA_HOME`'s JDK; there's no toolchain download.
- **Day 25 needs Java 8**: on Apple Silicon Gradle provisions the x64 Temurin 8 and runs it under Rosetta 2. **Day 28's Docker steps** were written without a running Docker engine: try them before the session.
- **Red on purpose** (generated from `course.json`): <!-- course:red -->starter tests red on purpose on Days 10, 16, 19–30; Bruno requests that fail on the starter until the work is done on Days 1–3, 10, 14–16, 19, 21–23, 25<!-- /course:red -->. Don't "fix" them.
- **Starters with TODO tests** (Days 4, 5, 7, 14, 15, 17, 18, 19, Days 20–23 where the learner writes tests, and Day 30) name every test the solution adds; the briefs list the same names.
- **Advisory counts (Days 8, 9, 12) are a snapshot from 2026-11-10.** They drift; the briefs say so. Re-run `tools/OsvCheck.java` to refresh.
- **OpenRewrite is pinned to 7.39.0 / BOM 3.37.0** (newer releases need an authenticated repository) and needs `org.gradle.jvmargs=-Xmx2g`, or the daemon can run out of memory and leave a ~465 MB `java_pid*.hprof`.
- **Day 12's solution ends with a Tomcat 10.1.x override:** Boot 3.5.16's open-source support has ended; Spring Framework 6.2 fixes are blocked until Boot 4.
- **Days 6–7** carry JUnit 4 tests run by the Vintage engine (realistic for 2022 code); Day 7 migrates them and removes Vintage. The test count must not change (`course.json` holds it).
- **Errors go through `ApiExceptionHandler`** (`@RestControllerAdvice`, `ProblemDetail`) from Day 1 on the modern app. New exceptions get an `@ExceptionHandler` there, not `@ResponseStatus`.
- **Demo users (Days 14–23 and 26):** ada / grace (CUSTOMER), teller (TELLER), passwords `<name>-pass`. Day 17 stores them as PBKDF2 hashes.
- **Security days detect and fix known problems; they never include exploit code** beyond the regression-test inputs.
