# Troubleshooting

Find your symptom, read across. Error texts are copied from this course's projects. New words: [glossary](glossary.md).

**Before anything else:** are you in the right folder? `pwd` should end in `day-NN-…/starter` (or `solution`), and `ls gradlew` should find the wrapper. Night-before check for a new laptop: `tools/doctor.sh` from the course root.

- [Terminal and Gradle](#terminal-and-gradle)
- [Running the app](#running-the-app)
- [HTTP status codes](#http-status-codes)
- [Tests](#tests)
- [Reading a stack trace](#reading-a-stack-trace)

<a id="terminal-and-gradle"></a>
## Terminal and Gradle

| Symptom | Cause | Fix |
|---|---|---|
| `zsh: no such file or directory: ./gradlew` | You're in the course root or the day folder, not in `starter/` | `cd day-NN-…/starter`, then `ls gradlew` |
| `zsh: permission denied: ./gradlew` | The wrapper lost its executable flag (common after copying or unzipping) | `chmod +x gradlew` |
| `command not found: gradle`, or a build that behaves oddly with `gradle …` | You ran a global Gradle (or none). Each project needs its own Gradle version: 9.8 on modern days, 8.14 on Days 6–13 | Always `./gradlew …`. [Gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) |
| `Task 'run' not found in root project 'accounts'.` | Spring Boot's task is called `bootRun` | `./gradlew bootRun` |
| `Task 'boot-run' not found in root project 'accounts'. Some candidates are: 'bootRun'.` | Task names are camelCase | Use the name in "candidates". `./gradlew tasks` lists them all |
| `Could not find or load main class` / `package org.springframework.boot does not exist` after `java AccountsApplication.java` | Plain `java` can't see the libraries Gradle downloaded | `./gradlew bootRun` |
| Days 6–13: `* What went wrong:` followed by only a version, e.g. `25.0.5` | Gradle 8.14 started on your JDK 25, which it can't run on. `gradle/gradle-daemon-jvm.properties` (which says `toolchainVersion=17`) is missing or renamed | Put the file back. On another old project without one, start Gradle on an older JDK, e.g. macOS: `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew build`. [Daemon JVM criteria](https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria) |
| Day 11: `java.lang.OutOfMemoryError: Java heap space` during `rewriteDryRun`, and a huge `java_pid….hprof` file appears | OpenRewrite loads hundreds of recipes, more than Gradle's default 512 MB | Add `org.gradle.jvmargs=-Xmx2g` to `gradle.properties` next to `build.gradle.kts`, run `./gradlew --stop`, delete the `.hprof`, try again |
| `Plugin [id: 'org.springframework.boot', version: '…'] was not found` or `No cached version … available for offline mode` | No network (or a proxy) on the first build of a project, so Gradle can't download | Build each day once while online; `tools/doctor.sh` checks Day 1 |
| `./gradlew build -x test` "fixes" a red build | It skips the tests, so it hides the problem | Never ship that. Fix the test, or the code |

<a id="running-the-app"></a>
## Running the app

| Symptom | Cause | Fix |
|---|---|---|
| `Web server failed to start. Port 8080 was already in use.` | Another app (often yesterday's `bootRun`, or another day's) holds the port | `lsof -i :8080`, then `Ctrl+C` in that terminal or `kill <PID>`. Only one day runs at a time |
| `Database may be already in use: ".../data/accounts.mv.db". Possible solutions: close all other connection(s); use the server mode [90020-…]` | A second copy of the same project (or an IDE run plus a terminal run) has the H2 file open | Stop the other one. If none is running, `./gradlew --stop` |
| `APPLICATION FAILED TO START` … `Parameter 0 of constructor in com.example.accounts.controller.AccountController required a bean of type 'com.example.accounts.service.AccountService' that could not be found.` | The class isn't a [bean](glossary.md#bean): a missing `@Service` / `@Component` / `@Repository`, or a class outside the `com.example.accounts` package | Add the annotation, or move the class under the main package. The "Action: Consider defining a bean" advice is rarely the real fix |
| After pulling a new day: a `WARN` at start-up mentioning `NULL not allowed for column "USERNAME"`, then 500s with `Column "A1_0.USERNAME" not found` | Day 15 adds a `NOT NULL` column; your `data/` file already has rows without it, so H2 can't add the column (`ddl-auto=update` only warns) | Stop the app, `rm -rf data`, start again. The seeder recreates the demo accounts |
| `Unsupported database file version or invalid file header in file ".../data/accounts.mv.db"` … `Caused by: org.h2.mvstore.MVStoreException: The write format 1 is smaller than the supported format 2` | The file was written by an older H2: 1.4 (the legacy pin `h2.version = 1.4.200`) can't be read by 2.x. You'll meet it on Day 9 when the pin goes, and again when an upgrade changes H2 (2.1 → 2.3 on Day 11) | `rm -rf data` (disposable on a laptop). A real service needs a [migration](https://www.h2database.com/html/migration-to-v2.html) |
| Odd data, a test account you deleted is back, Bruno results differ between runs | `data/` keeps everything between runs | Reset: stop, `rm -rf data`, start |

<a id="http-status-codes"></a>
## HTTP status codes

What the code means, and its most likely cause **in this course**. Every handled error is a [problem detail](glossary.md#problem-detail): read its `title` and `detail`. Reference: [MDN status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status).

| Code | Means | Most likely cause here |
|---|---|---|
| **400** Bad Request | The request is wrong | A validation failure (the body's `errors` names each field), an unsupported currency, insufficient funds (Day 19), malformed JSON, or a missing `?currency=` on `/accounts/mine` |
| **401** Unauthorized | Not logged in (or the wrong password) | No credentials on the Bruno request's **Auth** tab, or a typo. Users: `ada`/`ada-pass`, `grace`/`grace-pass`, `teller`/`teller-pass` (legacy days: `teller` only). See [401 vs 403](glossary.md#401-vs-403) |
| **403** Forbidden | Logged in, not allowed | A customer calling a teller-only endpoint (`GET /accounts`, `POST`, `PUT`, `DELETE`). In a `@WebMvcTest`: see [Tests](#tests). Security runs first, so a customer gets 403 even for a method that doesn't exist |
| **404** Not Found | No such thing | A wrong path (`/account`), an id that doesn't exist, or (from Day 15, on purpose) **someone else's** account: we don't confirm it exists |
| **405** Method Not Allowed | Right path, wrong HTTP method | e.g. `POST /accounts/1`. Check the mapping in `AccountController` |
| **409** Conflict | Clashes with the current state | Not produced by Days 1–19. Days 20–23 (concurrent updates, idempotency keys) are where it shows up |
| **415** Unsupported Media Type | The body isn't JSON as far as Spring can tell | Missing `Content-Type: application/json` (curl: add `-H "Content-Type: application/json"`; Bruno: Body → JSON) |
| **422** Unprocessable Content | Well-formed but semantically invalid | Not used by this course: our validation errors are 400. Some team APIs use 422; follow the contract |
| **500** Internal Server Error | An exception nobody handled | Read the server log, not the response (see [stack trace](#reading-a-stack-trace)). Classics: `.get()` on an empty `Optional` (`NoSuchElementException`, a Day 2 bug), a stale `data/` file, a `NullPointerException`. Give the exception an `@ExceptionHandler` in `ApiExceptionHandler` |

<a id="tests"></a>
## Tests

| Symptom | Cause | Fix |
|---|---|---|
| `No tests found for given includes: [AccountTest](--tests filter)` | The `--tests` filter doesn't match a real test class | Look in `src/test/java` for the name: `./gradlew test --tests AccountServiceTest`, or a wildcard: `--tests '*Transfer*'` |
| The terminal says `FAILED` but not why | Gradle prints a summary only | Open `build/reports/tests/test/index.html` in a browser and click through to the failure (assertion, stack trace, standard output) |
| A `@WebMvcTest` gets **403** on `POST`/`PUT`/`DELETE`, or a customer gets 200 where your rules say 403 | `@WebMvcTest` doesn't load `@Configuration` classes, so your `SecurityConfig` is ignored and Boot's defaults apply (CSRF on, any logged-in user allowed) | Add `@Import(SecurityConfig.class)` to the test class (and `@WithMockUser`). [Spring Boot docs](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html) |
| Green build, but **fewer tests** than before | JUnit 4 tests (`org.junit.Test`) compile but nothing runs them once the [vintage engine](glossary.md#vintage-engine) is gone and `junit:junit` is still on the classpath | Count before and after (the report's total). Migrate every JUnit 4 test before removing vintage: `grep -rn "org.junit.Test\|org.junit.Assert" src/test` must print nothing |
| Day 11: `cannot find symbol … MockBean` (or a deprecation warning) | `@MockBean` is deprecated in Boot 3.4+ and gone in Boot 4 | `@MockitoBean` from `org.springframework.test.context.bean.override.mockito` |
| A starter is red before you've changed anything | Some are **red on purpose**: <!-- course:red -->starter tests red on purpose on Days 10, 16, 19–30; Bruno requests that fail on the starter until the work is done on Days 1–3, 10, 14–16, 19, 21–23, 25<!-- /course:red --> | That's the task. Don't skip or delete the tests |

<a id="reading-a-stack-trace"></a>
## Reading a stack trace

A stack trace is the exception, then the chain of method calls that led to it, **newest first**. This one is real: someone "improved" `findAll` to return the first five accounts.

```
java.lang.IndexOutOfBoundsException: toIndex = 5                                       ← 1. WHAT
	at java.base/java.util.AbstractList.subListRangeCheck(AbstractList.java:509)
	at java.base/java.util.ArrayList.subList(ArrayList.java:1191)
	at com.example.accounts.service.AccountService.findAll(AccountService.java:21)      ← 2. WHERE (first line of ours)
	at com.example.accounts.controller.AccountController.getAll(AccountController.java:44)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(...)
	... about 50 more lines of Spring and Tomcat ...
```

1. **The top line says *what*:** the exception type and message (`toIndex = 5` on a list of 2).
2. **The first `com.example` line says *where*:** `AccountService.java`, line 21. Lines above it are the JDK; lines below are Spring calling us. Ignore both at first.
3. **The last `Caused by:` is the root cause.** Wrappers pile up on top; read to the bottom:

```
org.h2.jdbc.JdbcSQLNonTransientConnectionException: Unsupported database file version or invalid file header in file ".../data/accounts.mv.db" [90048-214]
	at org.h2.message.DbException.getJdbcSQLException(...)
	... many lines ...
Caused by: org.h2.mvstore.MVStoreException: The write format 1 is smaller than the supported format 2 [2.1.214/5]   ← the real problem
```

Read bottom-up: an old H2 1.4 file in `data/` ("write format 1") that H2 2.1 can't open. Fix: `rm -rf data`.

Start-up failures add a short report first: find `APPLICATION FAILED TO START` and read its **Description** line. More detail on a Gradle failure: `./gradlew build --stacktrace`, then `--info`. [Oracle: exceptions](https://docs.oracle.com/javase/tutorial/essential/exceptions/index.html)
