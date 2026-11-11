// Per-day additions to each brief, kept apart from the task text in briefs.js:
//   recall: 2–3 questions on EARLIER days, asked from memory at the start of the morning (answers at the end of the brief)
//   done:   the definition of done, one line
//   terms:  new words today (all defined in reference/glossary.md)
//   hints:  a 3-step ladder per task for the facilitator to release in the chat: nudge → where to look → nearly the answer
// build.js writes the brief sections and each day's hints.md from this file.

module.exports = {
  1: {
    recall: [
      ["Which annotation makes Spring run the `@NotBlank` checks on a request body?", "`@Valid` on the `@RequestBody` parameter (Day 0)."],
      ["Why does a client never send the `id` when creating an employee or account?", "The database generates it (`@GeneratedValue`). The request DTO doesn't have the field (Day 0)."],
    ],
    done: "`./gradlew build` is green and the Day 01 Bruno collection passes {{bruno.requests}}/{{bruno.requests}}, including the problem-detail titles.",
    terms: ["problem detail (RFC 9457)", "unchecked exception", "@RestControllerAdvice", "idempotent"],
    hints: [
      { task: "404 for a missing account", hints: [
        "Where does \"not found\" get decided today? Who should decide it?",
        "Look at `findById` in the service. `Optional` has a method that throws when it's empty.",
        "`accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id))`, a `RuntimeException` you create in an `exception` package.",
      ] },
      { task: "One error format for every endpoint", hints: [
        "You want one class that every controller's exceptions pass through.",
        "Search the Spring docs for `@RestControllerAdvice` and `@ExceptionHandler`, and look at `ProblemDetail.forStatusAndDetail`.",
        "Return a `ProblemDetail` from an `@ExceptionHandler(AccountNotFoundException.class)` method; set the title. For validation, handle `MethodArgumentNotValidException` and put `getFieldErrors()` into an `errors` property.",
      ] },
    ],
  },
  2: {
    recall: [
      ["What content type marks an RFC 9457 error body?", "`application/problem+json` (Day 1)."],
      ["Which is right for money: `double` or `BigDecimal`? And how do you build one?", "`BigDecimal`, from a `String` (`new BigDecimal(\"0.10\")`), never from a `double` (Day 0 reference sheet)."],
    ],
    done: "All five Bruno requests pass, the create response shows a balance of exactly `0.1`, and you can name the cause of each bug in one sentence.",
    terms: ["stack trace", "object identity vs equality", "safe HTTP method"],
    hints: [
      { task: "Bug 1 (GBP rejected)", hints: [
        "The log says GBP is unsupported. Read the check that decides it.",
        "How does Java compare two `String`s' contents? What does `==` compare?",
        "`c == \"GBP\"` compares object identity. Use `\"GBP\".equals(c)`, or better, a `Set.of(\"GBP\", \"EUR\", \"USD\").contains(c)`.",
      ] },
      { task: "Bug 2 (0.1 becomes 0.1000000000000000055…)", hints: [
        "Which type holds the opening balance in the request?",
        "`new BigDecimal(double)` copies the double's binary error exactly.",
        "Make `openingBalance` a `BigDecimal` in the DTO (with `@NotNull`) and pass it straight through.",
      ] },
      { task: "Bugs 3–6", hints: [
        "Compare each method with Day 1's solution: annotations, return types, mappings.",
        "Bug 3 is a missing annotation; bug 4 a return type; bug 5 an `Optional` method; bug 6 a mapping.",
        "`@Valid`; `ResponseEntity.created(uri).body(...)`; `orElseThrow(...)` not `.get()`; `@DeleteMapping(\"/{id}\")` + `@ResponseStatus(NO_CONTENT)`.",
      ] },
    ],
  },
  3: {
    recall: [
      ["What does `==` compare for two objects?", "Whether they are the same object, not the same contents (Day 2)."],
      ["Why must a `GET` never delete anything?", "GET is meant to be safe: browsers, crawlers and link previews send GETs freely (Day 2)."],
      ["What does `@RestControllerAdvice` give you?", "One place that turns exceptions from every controller into responses (Day 1)."],
    ],
    done: "The Day 03 collection passes {{bruno.requests}}/{{bruno.requests}}, it gave the same results after every refactor step (only the `XYZ` request changes, from 500 to 400), and no `@Autowired` field remains.",
    terms: ["refactoring", "constructor injection", "DTO", "layering"],
    hints: [
      { task: "Constructor injection", hints: [
        "A `final` field must be set by a constructor.",
        "Your IDE can convert a field to a constructor parameter (IntelliJ: Alt+Enter on the field).",
        "`private final AccountService accountService;` + a constructor taking it. Spring passes the bean in; remove `@Autowired`.",
      ] },
      { task: "Unsupported currency → 400", hints: [
        "The 500 comes from an `IllegalArgumentException`. Day 1 showed how to map an exception once.",
        "Create a specific exception for this case rather than mapping every `IllegalArgumentException`.",
        "`UnsupportedCurrencyException` thrown by the service + an `@ExceptionHandler` returning a 400 `ProblemDetail` titled `Unsupported currency`.",
      ] },
      { task: "Don't return the entity", hints: [
        "What would happen to the public JSON if someone added an internal column to `Account`?",
        "A record can carry exactly the fields the API promises.",
        "`record AccountResponse(Long id, String owner, String currency, BigDecimal balance)` with a `static from(Account)`; map in the controller.",
      ] },
    ],
  },
  4: {
    recall: [
      ["What is a DTO, and why do we return `AccountResponse` instead of `Account`?", "A data transfer object: the API contract stays independent of the database model (Day 3)."],
      ["Why is constructor injection easier to test?", "A test can call `new AccountService(fakeRepository)`, with no Spring (Day 3)."],
    ],
    done: "`./gradlew test` is green with **{{solution.tests}}** tests, and breaking the currency rule on purpose turns exactly the matching test red.",
    terms: ["unit test", "AssertJ", "mock", "Arrange / Act / Assert"],
    hints: [
      { task: "Mockito service tests", hints: [
        "The service needs a repository. In a unit test it gets a fake one.",
        "`@ExtendWith(MockitoExtension.class)`, `@Mock AccountRepository`, `@InjectMocks AccountService`; script it with `when(...).thenReturn(...)`.",
        "For \"never saved\": `verify(accountRepository, never()).save(any())`. For a saved account: `when(repo.save(any(Account.class))).thenAnswer(call -> call.getArgument(0))`.",
      ] },
      { task: "Comparing money in assertions", hints: [
        "`10.0` and `10.00` are different `BigDecimal`s to `equals`.",
        "AssertJ has an assertion that uses `compareTo`.",
        "`assertThat(balance).isEqualByComparingTo(\"10.00\")`.",
      ] },
    ],
  },
  5: {
    recall: [
      ["What do `@Mock` and `@InjectMocks` do?", "`@Mock` makes a fake; `@InjectMocks` builds the real class and hands it the fakes (Day 4)."],
      ["Which AssertJ method compares money safely?", "`isEqualByComparingTo` (Day 4)."],
    ],
    done: "`./gradlew test` is green with **{{solution.tests}}** tests; your 404 test checks the problem-detail title, not just the status.",
    terms: ["slice test", "@WebMvcTest", "MockMvc", "@MockitoBean", "@DataJpaTest", "test pyramid"],
    hints: [
      { task: "Controller tests", hints: [
        "You want HTTP, JSON and validation, but no database.",
        "`@WebMvcTest(AccountController.class)` + `@Autowired MockMvc` + `@MockitoBean AccountService`.",
        "`mockMvc.perform(get(\"/accounts/1\")).andExpect(status().isOk()).andExpect(jsonPath(\"$.owner\").value(\"Ada Lovelace\"))`; for a 400, finish with `verifyNoInteractions(accountService)`.",
      ] },
      { task: "Repository tests", hints: [
        "You want a real database, but only JPA.",
        "`@DataJpaTest` gives a fresh in-memory H2 and rolls back each test.",
        "`assertThat(accountRepository.count()).isZero()`; save, then `findById(saved.getId())` and `isPresent()`.",
      ] },
    ],
  },
  6: {
    recall: [
      ["Which test slice starts only the web layer?", "`@WebMvcTest` (Day 5)."],
      ["What replaced `@MockBean` in Spring Boot 4?", "`@MockitoBean` (Day 5). You'll see the old one today."],
    ],
    done: "The app builds and runs on Java 11, the Day 06 collection passes {{bruno.requests}}/{{bruno.requests}}, and `AUDIT.md` lists the platform plus at least five legacy markers.",
    terms: ["toolchain", "daemon JVM", "end of support", "legacy marker", "HTTP Basic"],
    hints: [
      { task: "The `What went wrong: 25.0.x` error", hints: [
        "Two different JDKs are involved in a Gradle build. Which one is too new?",
        "Look at `gradle/gradle-daemon-jvm.properties` and `./gradlew -version`.",
        "Gradle 8.14 itself can't run on Java 25; that file tells Gradle to run its daemon on JDK 17. The toolchain (11) is separate.",
      ] },
      { task: "Finding legacy markers", hints: [
        "Compare imports and annotations with Days 1–5.",
        "`grep -r \"javax\\.\" src`, `grep -r WebSecurityConfigurerAdapter src`, `grep -r \"org.junit.Test\" src`.",
        "`javax.*` (now `jakarta.*`), the security adapter, getter/setter DTOs (now records), JUnit 4, `@MockBean`, the `h2.version` pin.",
      ] },
    ],
  },
  7: {
    recall: [
      ["Which JDK runs Gradle, and which compiles the legacy code?", "Daemon JVM 17 runs Gradle; the toolchain JDK 11 compiles (Day 6)."],
      ["What does `@WithMockUser` do?", "Runs a test as if a user were logged in, without a real password (Day 6)."],
    ],
    done: "`./gradlew test` green with **{{solution.tests}}** tests after the migration, no `org.junit.Test` import left, and `grep -i vintage` on the test classpath prints nothing.",
    terms: ["JUnit 4", "JUnit Platform", "Vintage engine", "Jupiter"],
    hints: [
      { task: "Writing JUnit 4 style", hints: [
        "Copy the style of the tests already in the file.",
        "JUnit 4: `org.junit.Test`, `Assert.assertEquals(expected, actual)`, `@Test(expected = …)` or JUnit 4.13's `Assert.assertThrows`.",
        "`assertEquals(\"Account 99 not found\", ex.getMessage())` after `AccountNotFoundException ex = assertThrows(AccountNotFoundException.class, () -> service.getById(99L));`",
      ] },
      { task: "Migrating without losing tests", hints: [
        "Count the tests in the HTML report before you change anything.",
        "`@RunWith(MockitoJUnitRunner.class)` → `@ExtendWith(MockitoExtension.class)`; `@Before` → `@BeforeEach`; `Assert` → AssertJ.",
        "Remove the `junit-vintage-engine` dependency last, then check the count is still 9.",
      ] },
    ],
  },
  8: {
    recall: [
      ["Which engine runs JUnit 4 tests on the JUnit Platform?", "Vintage (Day 7)."],
      ["Which Gradle file pins the Gradle version?", "`gradle/wrapper/gradle-wrapper.properties` (Day 6)."],
    ],
    done: "`gradle.lockfile` exists with the build green, and `FINDINGS.md` has the totals, a top-findings table and where each comes from.",
    terms: ["transitive dependency", "lockfile", "CVE / advisory", "OSV", "dependencyInsight"],
    hints: [
      { task: "Writing the lockfile", hints: [
        "Gradle has a feature called dependency locking.",
        "Add `dependencyLocking { lockAllConfigurations() }` to `build.gradle.kts`.",
        "Then `./gradlew dependencies --write-locks` writes `gradle.lockfile`.",
      ] },
      { task: "Tracing a library", hints: [
        "You want to know *why* a version is on the classpath.",
        "`./gradlew -q dependencyInsight --dependency <name> --configuration runtimeClasspath`.",
        "Read the output bottom-up: the last line is what pulled it in. \"Selected by rule\" means a version property, possibly ours.",
      ] },
    ],
  },
  9: {
    recall: [
      ["Why do scanners need a lockfile rather than `build.gradle.kts`?", "They need resolved versions, including transitive ones (Day 8)."],
      ["Which finding did we cause ourselves?", "The `h2.version` pin from 2021 (Day 8)."],
    ],
    done: "`./gradlew build` green, every override has a reason comment, the re-scan shows no CRITICAL in Tomcat or H2, and every top finding has a decision.",
    terms: ["version override", "patch bump vs line jump", "accepted risk"],
    hints: [
      { task: "Red build after the overrides", hints: [
        "Read the first line of the error: what does Gradle say it didn't resolve?",
        "The lockfile still records the old versions.",
        "`./gradlew dependencies --write-locks`, then build again.",
      ] },
      { task: "Second red build (contextLoads)", hints: [
        "The error mentions the database file version.",
        "The H2 version changed, and `data/` was written by the old one.",
        "On a laptop: `rm -rf data`. In production you'd export with the old version and import with the new one.",
      ] },
    ],
  },
  10: {
    recall: [
      ["How do you override a version Spring Boot manages?", "Set its version property, e.g. `extra[\"tomcat.version\"] = \"9.0.123\"` (Day 9)."],
      ["Why couldn't Day 9 fix every CRITICAL?", "Spring Framework 5.3 and Security 5.7 are out of open-source support; the fixes are only in newer lines (Day 9)."],
    ],
    done: "`./gradlew test` green (the 3 regression tests now pass) and the Day 10 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["actuator", "profile", "defence in depth", "secret rotation"],
    hints: [
      { task: "Hide actuator endpoints", hints: [
        "Two separate things: which endpoints exist over HTTP, and who may call them.",
        "`management.endpoints.web.exposure.include`, and the matchers in `SecurityConfig`.",
        "`include=health,info`, and `.antMatchers(\"/actuator/health\", \"/actuator/info\").permitAll()` instead of `/actuator/**`.",
      ] },
      { task: "H2 console only in dev", hints: [
        "Spring can load extra properties for a named profile.",
        "`application-dev.properties` is read only when the `dev` profile is active.",
        "Move `spring.h2.console.enabled=true` there; delete both console lines from `application.properties`.",
      ] },
    ],
  },
  11: {
    recall: [
      ["What are the two layers of the actuator fix?", "Don't expose endpoints, and don't let strangers reach the ones you do expose (Day 10)."],
      ["What does `extra[\"spring-security.version\"]` do?", "Overrides the Spring Security version Boot would pick (Day 9). Remember it today."],
    ],
    done: "`./gradlew build` green on Java 17 / Boot 3.5.16, no OpenRewrite or Boot 2.7 overrides left, and the Day 11 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["OpenRewrite", "recipe", "Jakarta vs javax", "dispatcher type"],
    hints: [
      { task: "`requestMatchers` doesn't compile", hints: [
        "The code was rewritten for Spring Security 6. Which version is the compiler actually using?",
        "`./gradlew -q dependencyInsight --dependency spring-security-config --configuration compileClasspath`.",
        "Our own Day 9 overrides still pin the 5.x lines. Delete the whole override block; Boot 3.5 manages newer versions.",
      ] },
      { task: "A 404 became a 401", hints: [
        "The test report says what changed. Which part of the request handling runs after the 404?",
        "Spring Security 6 also authorizes the forward to the `/error` page.",
        "`.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` in the security rules.",
      ] },
    ],
  },
  12: {
    recall: [
      ["What did OpenRewrite not know about?", "Our own version overrides from Day 9 (Day 11)."],
      ["What does a lockfile do when resolved versions change?", "Fails the build until you regenerate it (Day 8)."],
    ],
    done: "`FINDINGS.md` has a post-migration totals row, every \"needs Boot 3\" item is closed or re-decided, and any patchable finding is patched with a reason comment.",
    terms: ["re-scan"],
    hints: [
      { task: "Why CRITICALs came back", hints: [
        "Look at the release date of Boot 3.5.16 against today's date.",
        "Check spring.io's support dates for 3.5.",
        "3.5.16 is the last open-source 3.5 release; advisories published since then have no fix on that line.",
      ] },
      { task: "Patch what a patch can still fix", hints: [
        "Same trick as Day 9.",
        "Look up the fixed version on osv.dev, then check Maven Central for a newer release in the same line.",
        "`extra[\"tomcat.version\"] = \"10.1.x\"` with a reason comment; regenerate the lockfile; re-scan.",
      ] },
    ],
  },
  13: {
    recall: [
      ["Name two things you check before any upgrade.", "e.g. which JDK runs Gradle (`./gradlew -version`), the toolchain, and dependencies with their own JDK requirements (Days 6, 11)."],
      ["Why did the scanner still show findings after Day 11?", "Boot 3.5's open-source support had ended (Day 12)."],
    ],
    done: "`./gradlew build` green, the log says `using Java 21`, `gradle.lockfile` is unchanged, and your ticket lists every change outside `build.gradle.kts`.",
    terms: ["jdeps", "JEP", "LTS"],
    hints: [
      { task: "Which JDK does what", hints: [
        "There are three: your shell's, Gradle's and the compiler's.",
        "`java -version`, `./gradlew -version`, `./gradlew -q javaToolchains`.",
        "Shell: 25; Gradle daemon: 17 (from `gradle-daemon-jvm.properties`); toolchain: whatever `JavaLanguageVersion.of(...)` says.",
      ] },
      { task: "Is 21 supported?", hints: [
        "Primary sources only.",
        "Spring Boot 3.5's system requirements page, and Gradle's compatibility matrix.",
        "Boot 3.5: Java 17 to 25. Gradle 8.14: Java 21 toolchains are supported, so the daemon can stay on 17.",
      ] },
    ],
  },
  14: {
    recall: [
      ["In the legacy `SecurityConfig`, what do `permitAll()` and `authenticated()` mean?", "Anyone may call it; you must log in first (Day 6)."],
      ["Which test slice would you use to test a 403?", "`@WebMvcTest` (Day 5)."],
    ],
    done: "`./gradlew test` green including your four new tests, and the Day 14 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["SecurityFilterChain", "deny by default", "401 vs 403", "CSRF", "role"],
    hints: [
      { task: "Rules in the right order", hints: [
        "Rules are checked top to bottom.",
        "Put the specific rule (`GET /accounts/{id}`) before the general one (`/accounts/**`).",
        "`.requestMatchers(\"/hello\").permitAll()`, `.requestMatchers(HttpMethod.GET, \"/accounts/{id}\").hasAnyRole(\"CUSTOMER\", \"TELLER\")`, `.requestMatchers(\"/accounts/**\").hasRole(\"TELLER\")`, `.anyRequest().authenticated()`.",
      ] },
      { task: "Controller tests break", hints: [
        "A 403 on POST in a test, before your rules even apply, suggests different rules are running.",
        "`@WebMvcTest` doesn't load your `SecurityConfig` unless you ask.",
        "`@Import(SecurityConfig.class)` and `@WithMockUser(roles = \"TELLER\")` on the class; `@WithAnonymousUser` for the 401 test.",
      ] },
    ],
  },
  15: {
    recall: [
      ["401 or 403: which means \"you can't do that\"?", "403 (Day 14)."],
      ["Where does business logic belong: controller or service?", "The service (Day 3)."],
    ],
    done: "Ada gets 404 for account 2, tellers see any account, your three service tests pass, and the Day 15 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["IDOR", "ownership check", "Authentication (Spring Security)"],
    hints: [
      { task: "Who is calling?", hints: [
        "Spring can give a controller method the logged-in user.",
        "Add an `Authentication` parameter; `getName()` is the username, `getAuthorities()` the roles.",
        "Pass `caller.getName()` and an `isTeller` flag to a service method that checks `account.getUsername()`.",
      ] },
      { task: "404 not 403", hints: [
        "What would a 403 tell an attacker?",
        "It confirms that account 2 exists.",
        "Throw the same `AccountNotFoundException` you use for missing accounts.",
      ] },
    ],
  },
  16: {
    recall: [
      ["What is an IDOR?", "Reading or changing someone else's record by changing its id (Day 15)."],
      ["Which Spring Data feature writes SQL from a method name?", "Query methods: name it `findByCurrency` and Spring Data writes the SQL (Day 5)."],
    ],
    done: "`AccountSearchTest` green, no `+` builds a query anywhere, and the Day 16 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["injection", "bound parameter", "JPQL / HQL", "allow-list"],
    hints: [
      { task: "Why the test fails", hints: [
        "Write out the query string with the test's input pasted in.",
        "`... AND a.currency = 'GBP' OR '1'='1'`. What does `OR '1'='1'` do to the WHERE?",
        "It's always true, so every account matches, including Grace's.",
      ] },
      { task: "The fix", hints: [
        "Don't build the query string yourself.",
        "Spring Data can write this query from a method name, with bound parameters.",
        "A query method on `AccountRepository` whose name says both conditions (username *and* currency); then the `EntityManager` code has nothing left to do."
      ] },
    ],
  },
  17: {
    recall: [
      ["How do you fix injection: escaping or bound parameters?", "Bound parameters (Day 16)."],
      ["Where did the legacy app keep a secret, and what did we do?", "In `application.properties`; moved it to an environment variable and noted it must be rotated (Day 10)."],
    ],
    done: "No `{noop}` passwords remain, all four `PasswordStorageTest` tests pass, and logins still work (Day 17 collection {{bruno.requests}}/{{bruno.requests}}).",
    terms: ["hashing vs encryption", "salt", "work factor", "PBKDF2", "DelegatingPasswordEncoder"],
    hints: [
      { task: "A password encoder", hints: [
        "Spring Security uses a `PasswordEncoder` bean for every login.",
        "`DelegatingPasswordEncoder` reads the `{id}` prefix to choose the algorithm.",
        "`new DelegatingPasswordEncoder(\"pbkdf2\", Map.of(\"pbkdf2\", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()))` as a `@Bean`.",
      ] },
      { task: "Getting the hashes", hints: [
        "You need to encode each demo password once.",
        "A throwaway test can print `passwordEncoder().encode(\"ada-pass\")`.",
        "Run it with `./gradlew test --tests '*YourTest*' -i`, copy the `{pbkdf2}…` values into `SecurityConfig`, delete the test.",
      ] },
    ],
  },
  18: {
    recall: [
      ["Why is a password hashed rather than encrypted?", "A hash can't be reversed, so a leak doesn't reveal the passwords (Day 17)."],
      ["Which test slice did you use for controller rules?", "`@WebMvcTest` with `@Import(SecurityConfig.class)` (Day 14)."],
    ],
    done: "The four log tests pass, the attempted password appears in no log, and both audit lines show in the terminal when you run the Day 18 collection.",
    terms: ["audit log", "log injection", "OutputCaptureExtension", "@EventListener"],
    hints: [
      { task: "Hearing about failed logins", hints: [
        "Spring Security publishes an event when a login fails.",
        "`@EventListener` on a method taking `AbstractAuthenticationFailureEvent`, in a `@Component`.",
        "Log `event.getAuthentication().getName()` and `event.getException().getClass().getSimpleName()`. Never the credentials.",
      ] },
      { task: "No fake log lines", hints: [
        "What could a username containing a newline do to a log file?",
        "Clean caller-supplied values before logging them.",
        "Replace `\\r` and `\\n` with `_` in a small helper used by every audit line.",
      ] },
    ],
  },
  19: {
    recall: [
      ["What must never appear in an audit log?", "Passwords (even wrong ones) and uncleaned caller input (Day 18)."],
      ["Why is a `@DataJpaTest` rolled back after each test?", "Each test runs in its own transaction (Day 5). That matters today."],
    ],
    done: "`TransferTest` green including your insufficient-funds test, and the Day 19 collection passes {{bruno.requests}}/{{bruno.requests}} on a fresh database.",
    terms: ["transaction", "rollback", "fail closed", "@Transactional"],
    hints: [
      { task: "Make it all-or-nothing", hints: [
        "Two changes must succeed or fail together.",
        "Spring wraps a method in a database transaction when you ask.",
        "`@Transactional` on `transfer`: if a `RuntimeException` escapes, every change is rolled back.",
      ] },
      { task: "Check before you change", hints: [
        "In which order does the colleague's code load and change things?",
        "Load and check both accounts before touching either balance.",
        "Move `getById(toId)` above `from.withdraw(amount)`.",
      ] },
    ],
  },
  20: {
    recall: [
      ["What does `@Transactional` guarantee if the second step of a transfer fails?", "Everything rolls back: all or nothing (Day 19)."],
      ["Why would a `@DataJpaTest` have hidden Day 19's bug?", "Each test runs inside its own transaction that's rolled back, so you never see what the service commits (Day 19)."],
      ["Which status means \"you can't do that\" for a logged-in user?", "403 (Day 14)."],
    ],
    done: "`ConcurrentTransferTest` and your `aConcurrentUpdateIs409` pass ({{solution.tests}} tests in total), and the Day 20 collection passes {{bruno.requests}}/{{bruno.requests}} on a fresh database.",
    terms: ["lost update", "optimistic locking", "@Version", "409 Conflict"],
    hints: [
      { task: "Make the second commit fail", hints: [
        "The database needs a way to notice that the row changed since you read it.",
        "JPA has an annotation for a column that changes on every update and is checked when you write.",
        "A `Long` field on `Account` with that annotation. Then look at the SQL Hibernate writes for an update: what does the extra condition do to a stale write?",
      ] },
      { task: "409 instead of 500", hints: [
        "What exception reaches the controller when the commit fails?",
        "Spring translates Hibernate's exception into its own `OptimisticLockingFailureException` (a parent of `ObjectOptimisticLockingFailureException`).",
        "Handle Spring's exception (not Hibernate's) in `ApiExceptionHandler`, the same way as your other handlers, with the status and title the brief asks for.",
      ] },
    ],
  },
  21: {
    recall: [
      ["What does `@Version` do on a write?", "Adds `AND version = ?` to the update, so a stale write fails instead of overwriting (Day 20)."],
      ["Which HTTP methods are idempotent by definition: GET, PUT, DELETE or POST?", "GET, PUT and DELETE. POST isn't, which is today's problem (Day 1)."],
    ],
    done: "`IdempotencyTest` is fully green including `keysAreScopedPerUser`, every existing test still passes, and the Day 21 collection passes {{bruno.requests}}/{{bruno.requests}} on a fresh database.",
    terms: ["idempotency", "idempotency key", "request fingerprint", "422 Unprocessable Content"],
    hints: [
      { task: "Remember completed payments", hints: [
        "You need to look up \"have I already done this payment for this user?\"",
        "A small entity keyed by (username, key) with the request's fingerprint and the status you answered. `key` is a reserved SQL word: name the column something else.",
        "In `transfer`: find the record; if it exists, compare fingerprints (different → 422, same → return the stored status); otherwise transfer and save the record, all inside the same `@Transactional` method.",
      ] },
      { task: "Same request, different spelling", hints: [
        "Is `50` the same amount as `50.00`?",
        "Build the fingerprint from the meaning (from, to, amount), not the raw body text.",
        "`\"from=\" + from + \" to=\" + to + \" amount=\" + amount.stripTrailingZeros().toPlainString()`.",
      ] },
      { task: "400 with our title", hints: [
        "If Spring rejects a missing header itself, the title isn't ours.",
        "`@RequestHeader(value = \"Idempotency-Key\", required = false)`.",
        "Check it in the controller and throw your own `IdempotencyKeyRequiredException` → 400 `Idempotency key required`.",
      ] },
    ],
  },
  22: {
    recall: [
      ["Why must the idempotency key be saved in the same transaction as the transfer?", "Otherwise a crash between them leaves a key without a transfer, or a transfer without a key (Day 21)."],
      ["Why build `BigDecimal` from a `String`, and compare with `compareTo`?", "A `double` is already inexact; `equals` also compares scale, so 10.5 ≠ 10.50 (Day 2, `reference/java-idioms.md`)."],
    ],
    done: "`MoneyRulesTest` and your five `MoneyTest` tests pass ({{solution.tests}} tests in total), and the Day 22 collection passes {{bruno.requests}}/{{bruno.requests}} on a fresh database.",
    terms: ["ISO 4217", "scale", "rounding mode", "value object"],
    hints: [
      { task: "A Money type", hints: [
        "An amount without its currency is half the information.",
        "A `record Money(BigDecimal amount, String currency)` with a compact constructor that checks its inputs.",
        "In the compact constructor: `amount = amount.setScale(2, RoundingMode.UNNECESSARY)`. That throws on 10.005 instead of rounding; `add`/`subtract` throw `CurrencyMismatchException` when the currencies differ.",
      ] },
      { task: "Half a penny at the edge", hints: [
        "Bean Validation can limit digits before and after the decimal point.",
        "`@Digits(integer = …, fraction = …)` on the request field.",
        "`@Digits(integer = 17, fraction = 2)` matches the `precision = 19, scale = 2` column.",
      ] },
    ],
  },
  23: {
    recall: [
      ["What must never appear in an audit log?", "Passwords, and uncleaned caller input (Day 18)."],
      ["Why does `Money` refuse to round 10.005?", "Silent rounding creates or loses money; the caller must decide (Day 22)."],
    ],
    done: "Both red tests and your three `MaskTest` tests pass ({{solution.tests}} tests in total), the Day 23 collection passes {{bruno.requests}}/{{bruno.requests}}, and the transfer log line in the terminal shows only `****` numbers.",
    terms: ["PII", "masking", "data minimisation"],
    hints: [
      { task: "Mask in one place", hints: [
        "Two places show account numbers: responses and a log line. How do you stop them drifting apart?",
        "A small static helper next to Day 18's `AuditLog`.",
        "`Mask.accountNumber(n)`: null stays null; fewer than 4 characters → `\"****\"`; otherwise `\"****\" + n.substring(n.length() - 4)`. Call it from `AccountResponse.from` and the log line.",
      ] },
      { task: "Which responses?", hints: [
        "How many endpoints return an account?",
        "Every one of them builds its JSON the same way.",
        "`AccountResponse.from` is the single exit point: mask there, not in the entity (the stored number must stay intact).",
      ] },
    ],
  },
  24: {
    recall: [
      ["How did you move a Boot-managed library to a patched version in Gradle?", "Set the version property Boot reads, e.g. `extra[\"logback.version\"] = \"1.2.13\"`, rather than adding a version to the dependency (Day 9)."],
      ["What's a transitive dependency, and how did you find who brings one in?", "A library you get because another library needs it. `./gradlew dependencyInsight --dependency <name>` shows the path (Day 8)."],
      ["What does a `@WebMvcTest` load, and what does it leave out?", "The web layer only (controllers, advice, MockMvc); no services or repositories, which you replace with mocks (Day 5)."],
    ],
    done: "`./mvnw test` runs {{solution.tests}} tests with 0 failures, `./mvnw dependency:tree` shows `jackson-databind` and `jackson-core` at 3.1.8, and `pom.xml` differs from the starter by one `<properties>` entry.",
    terms: ["Maven", "POM", "Maven Wrapper", "BOM", "effective POM"],
    hints: [
      { task: "Find Jackson in the tree", hints: [
        "Maven's equivalent of `dependencies` is a goal of the dependency plugin.",
        "`./mvnw dependency:tree` prints everything; the Maven docs page for `dependency:tree` lists a parameter that filters by group or artifact.",
        "`./mvnw dependency:tree -Dincludes=tools.jackson.core` (Jackson 3's group id is `tools.jackson.core`, not `com.fasterxml.jackson.core`). Read the path from the top: which of *your* dependencies is first in the chain?",
      ] },
      { task: "Which property?", hints: [
        "Boot doesn't manage every Jackson module one by one; it imports Jackson's own BOM.",
        "Spring Boot's \"Version Properties\" appendix lists every property it reads; or search the effective POM (`./mvnw help:effective-pom -Doutput=target/effective-pom.xml`) for `jackson`.",
        "The property is named after the BOM, not after `jackson-databind`. There is no `jackson-databind.version`.",
      ] },
      { task: "Override it the Maven way", hints: [
        "In Gradle you set `extra[\"…\"]`. Maven's equivalent is a block you already have in `pom.xml`.",
        "Look at how `<java.version>` is set. Boot's docs show the same trick with `<slf4j.version>`.",
        "Add one entry next to `<java.version>` in `<properties>`, then re-run `dependency:tree -Dincludes=tools.jackson.core`: both `jackson-databind` and `jackson-core` should change.",
      ] },
    ],
  },
  25: {
    recall: [
      ["Why did `javax.persistence` become `jakarta.persistence`?", "Java EE moved to the Eclipse Foundation as Jakarta EE, and from Jakarta EE 9 the packages are `jakarta.*`; Boot 3 uses those (Day 11)."],
      ["Which three JDKs did you identify, and which one does `JavaLanguageVersion.of(...)` change?", "Your shell's (`java -version`), Gradle's daemon (`./gradlew -version`, pinned to 17 by `gradle-daemon-jvm.properties`), and the toolchain that compiles and tests the code. The toolchain line changes only the last (Day 13)."],
      ["What's a transitive dependency?", "A library you get because another library needs it; `dependencyInsight` shows who brings it in (Day 8)."],
    ],
    done: "`./gradlew build` is green on Java 11 ({{solution.tests}} tests, 0 failures), only `build.gradle.kts` changed, and the Day 25 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["JAXB", "JEP 320", "API jar vs implementation", "Rosetta 2", "Java version string"],
    hints: [
      { task: "Why did it break?", hints: [
        "Read the first compile error, not the last one. Which package is missing?",
        "That package used to be part of the JDK. Search for \"JEP 320\" and read its list of removed modules.",
        "`java.xml.bind` (JAXB) is in that list. The fix is to supply it yourself as dependencies, as JEP 320's \"Risks and Assumptions\" section suggests.",
      ] },
      { task: "Which coordinates and versions?", hints: [
        "Don't pick a version from a search engine: the framework already chose one.",
        "Spring Boot 2.7.18's \"Dependency Versions\" appendix lists every JAXB-related artifact it manages; look for `xml.bind` and `jaxb`.",
        "You need two: `jakarta.xml.bind:jakarta.xml.bind-api` (2.3.3: the \"jakarta\" coordinates, but still the `javax.xml.bind` package) and an implementation, `org.glassfish.jaxb:jaxb-runtime` (2.3.9). Leave the versions out.",
      ] },
      { task: "It compiles but the tests fail", hints: [
        "Read the exception in `build/reports/tests/test/index.html`. Is it a compile problem or a run-time one?",
        "`Implementation of JAXB-API has not been found` means the API is there but nothing implements it.",
        "Add the implementation with `runtimeOnly(...)`. Your code never imports it, so it doesn't belong on the compile classpath.",
      ] },
    ],
  },
  26: {
    recall: [
      ["A secret was committed to Git and you deleted the line. Is it safe now?", "No. It's still in the history: rotate it (issue a new one, revoke the old) (Day 10)."],
      ["Why can't `findByUsernameAndCurrency(username, currency)` be injected?", "Spring Data writes a fixed query and sends the values as bound parameters, never as query text (Day 16)."],
      ["Why do passwords need PBKDF2 (or bcrypt/Argon2) and not one SHA-256?", "Passwords are short and guessable, so the hash must be slow and salted; a fast hash can be brute-forced (Day 17)."],
    ],
    done: "`./gradlew test` passes all {{solution.tests}} tests, `TRIAGE.md` has a decision and justification for all six findings, and `reports/decisions/` has a complete record for each false positive and accepted risk.",
    terms: ["SAST", "SCA", "SARIF", "false positive", "triage"],
    hints: [
      { task: "Triage the six findings", hints: [
        "Read the code at each location before deciding anything. The tool sees patterns; you see what the code does.",
        "Two findings point at the same line. Two others are in places that never run in production or never build a query from text. Compare `locations`, and look at `src/test` vs `src/main`.",
        "SAST-103: find the Day 16 test that tries to inject through `currency`; that's your evidence. SAST-104: is test code packaged into the jar? SAST-105 is real but a dev tool: what limits the damage today, and what would make you fix it?",
      ] },
      { task: "`everyFixRemovedTheFlaggedCode` is red", hints: [
        "A Fix is a claim about the code, and the test checks it. Read its message: which finding, which file?",
        "Each SARIF result's `region.snippet` is the code the scanner flagged. Is that code still in the file?",
        "Either change the code so the flagged line is really gone, or the decision isn't Fix: make it a False positive or an Accept risk, with a record in `reports/decisions/`.",
      ] },
      { task: "Get the key out of the code", hints: [
        "The value must come from outside the code, and the test sets it through a property.",
        "Spring can inject a property into a constructor parameter. Which property name does `NoHardcodedSecretsTest` set?",
        "A constructor parameter `@Value(\"${notifications.api-key}\") String apiKey`, and in `application.properties` a line that reads it from `NOTIFICATIONS_API_KEY` with an empty default, so the app still starts locally. Then think about rotation.",
      ] },
      { task: "Replace MD5", hints: [
        "`TokenHashingTest` tells you which algorithm it expects.",
        "The algorithm is a string passed to `MessageDigest.getInstance`. The Java docs list the standard names.",
        "`\"SHA-256\"`. Why is one fast hash fine here but not for passwords? (Tokens are long and random.) Existing MD5 hashes can't be converted: the tokens must be reissued.",
      ] },
    ],
  },
  27: {
    recall: [
      ["What's a transitive dependency, and how do you find out why one is on the classpath?", "A library you get because another library needs it; `./gradlew dependencyInsight --dependency <name>` shows the path (Day 8)."],
      ["Why scan `runtimeClasspath` rather than everything?", "It's what you ship; test-only libraries don't reach production (Day 8)."],
      ["What goes in a recorded decision?", "What you found, what you did or why not, and evidence someone else can check (Days 9 and 26)."],
    ],
    done: "`./gradlew test` passes all {{solution.tests}} tests, `dependencyInsight` shows a fixed `log4j-core` in payments and statements, and `INCIDENT.md` names the affected services, the commands, the fix and the evidence.",
    terms: ["zero-day", "Log4Shell", "multi-project build", "version catalog", "blast radius"],
    hints: [
      { task: "Which services ship log4j-core?", hints: [
        "Look at what each service ships, not at what it mentions. None of the services' build files say \"log4j-core\".",
        "`dependencyInsight` per project, on the configuration that's packaged: `runtimeClasspath`. A project's tasks are addressed by its path, e.g. `:services:payments:`.",
        "`./gradlew -q :services:statements:dependencyInsight --dependency log4j-core --configuration runtimeClasspath`. Then try notifications with `--dependency org.apache.logging.log4j`: what does it ship instead, and is that jar affected?",
      ] },
      { task: "Upgrade it", hints: [
        "Ship a current release, and be ready to show it fixes *every* Log4j CVE from December 2021, not just the first one.",
        "Look on Maven Central for the newest 2.x release (not a beta). Apache's security page and NVD list the fixed versions per CVE: is your release past all of them?",
        "The version catalog has a `log4j` entry. Did changing it turn every test green? If not, search every `build.gradle.kts` for `2.14.1`: the estate needs one version, set in one place.",
      ] },
      { task: "Prove and report", hints: [
        "The tests read the version from the jar that's actually loaded. Read their failure message.",
        "Re-run `dependencyInsight` after the fix and paste the before and after into `INCIDENT.md`.",
        "In section 5, say why notifications isn't affected, with the test that proves it, and remind the reader that nothing is fixed until the new build is deployed.",
      ] },
    ],
  },
  28: {
    recall: [
      ["Which three JDKs did you find, and which one compiles the code?", "The shell's (`java -version`), the Gradle daemon's (`gradle-daemon-jvm.properties`, 17) and the toolchain's (`JavaLanguageVersion.of(21)`); the toolchain compiles and runs the tests (Day 13). Today adds a fourth: the Java runtime in the image, which runs production."],
      ["What did the dependency scan read, and what couldn't it see?", "`gradle.lockfile`, so only Java libraries; not the Java runtime or OS packages in the image (Day 8)."],
      ["After the Boot 3.5 migration, why did the re-scan still show findings?", "Boot 3.5's open-source support had ended, so some fixes needed a version override, such as the Tomcat 10.1.x patch (Day 12). A supported line is the precondition for getting patches."],
    ],
    done: "`./gradlew build` is green with {{solution.tests}} tests including the three `DockerfileTest` checks, the `Dockerfile` explains its base image choice, and (with Docker) `docker run --entrypoint id` shows a non-root uid.",
    terms: ["base image", "image tag", "digest", "non-root user", "multi-stage build"],
    hints: [
      { task: "The right base image", hints: [
        "Two things are wrong with `FROM openjdk:11-jre-slim`. Read the test failures: each names one.",
        "The `openjdk` page on Docker Hub (read the top of it), then the \"Supported tags\" list on the `eclipse-temurin` page. Which number does the toolchain in `build.gradle.kts` use?",
        "An `eclipse-temurin` JRE tag that starts with 21 and names an Ubuntu release: `21-jre-` followed by a codename from the tag list. Any other image (even one tagged `21…`) isn't a Java runtime image the test accepts.",
      ] },
      { task: "Not root", hints: [
        "Docker runs the container as the last `USER` in the final stage. With none, the process is root.",
        "Docker's best-practices page, section USER. You need a user that exists in the image, then a `USER` line (by convention just before `ENTRYPOINT`).",
        "`RUN groupadd … && useradd …` creating an `app` user with a fixed uid, then `USER <uid>:<gid>`. Numbers work better than names for Kubernetes `runAsNonRoot`.",
      ] },
      { task: "It starts, then fails to write", hints: [
        "What does the app write to disk, and where?",
        "`spring.datasource.url` in `application.properties` is a relative path. Relative to which folder in the container? Who owns it?",
        "`WORKDIR /app`, and create `/app/data` owned by your app user in the same `RUN` as `useradd`. Leave the jar itself owned by root.",
      ] },
    ],
  },
  29: {
    recall: [
      ["`./gradlew -version` says \"Daemon JVM: Compatible with Java 17\", but the tests run on Java 21. Why two JDKs?", "The toolchain picks the JDK that compiles and runs the tests; `gradle-daemon-jvm.properties` picks the JDK that runs Gradle itself. Gradle 8.14 can't run on Java 25, so it was pinned to 17 (Day 13)."],
      ["Why does the starter's build file set `extra[\"tomcat.version\"]`, and when should it go?", "Boot 3.5.16 is out of open-source support, so the latest 10.1.x Tomcat was forced by hand. Its comment says to remove it once Boot manages that version or newer (Day 12)."],
      ["What did OpenRewrite's patch miss on the Boot 2.7 → 3.5 upgrade?", "Our `extra[...]` version overrides, the lockfile, and behaviour changes: the tests had to catch those (Day 11)."],
    ],
    done: "`./gradlew build` is green with {{solution.tests}} tests (both `PlatformVersionTest` tests included), the Day 29 collection passes {{bruno.requests}}/{{bruno.requests}} against `bootRun`, the dev-profile H2 console still answers 200, and `BOOT4-UPGRADE.md` lists every change with its exact error and fix.",
    terms: ["Spring Boot starter", "test starter", "classic starters", "Jackson 3", "liveness / readiness probe"],
    hints: [
      { task: "Get it to compile", hints: [
        "Change one thing, run `./gradlew build`, read the *first* error. Write it down before you fix it.",
        "\"not part of the dependency lock state\" isn't a code problem: it's Day 8's lockfile doing its job. For missing packages, look at the guide's \"Test Code\" section and the starters table.",
        "Classes like `WebMvcTest` and `TestRestTemplate` now live under `org.springframework.boot.<technology>…` in their own jars. Compare with `day-23-mask-what-you-show/solution/build.gradle.kts` and its test imports, and search the guide for \"TestRestTemplate\".",
      ] },
      { task: "Make the tests pass", hints: [
        "If a test can't find a bean it used to get for free, ask what `@SpringBootTest` stopped providing in Boot 4.",
        "The guide's \"Upgrading Testing Features\" section covers MockMvc and `TestRestTemplate` with `@SpringBootTest`.",
        "There's an `@AutoConfigure…` annotation for each of them; one of our tests already has the MockMvc one.",
      ] },
      { task: "Green isn't done", hints: [
        "Check what you actually got, not what you asked for: the Tomcat version, and every feature the app had before (including in the `dev` profile).",
        "`./gradlew dependencyInsight --dependency tomcat-embed-core --configuration runtimeClasspath`, then read the selection reasons. For the dev profile: run it and try the URL from `application-dev.properties`.",
        "Something in `build.gradle.kts` from Day 12 is now holding a library *back*. And a feature that used to come with the web starter now needs its own module: the modern days' build file lists it.",
      ] },
    ],
  },
  30: {
    recall: [
      ["What did `extra[\"h2.version\"] = \"1.4.200\"` do in the legacy service, and why was it risky?", "It overrode the version Boot's dependency management picks, pinning an old, vulnerable H2 (Days 6 and 9). Watch for the opposite today: Boot choosing an older version than you had."],
      ["Which test slice loads only the web layer, and what must you `@Import` yourself?", "`@WebMvcTest`; your own `@Configuration` classes and the beans the controller needs (Day 5, and Day 14's `@Import(SecurityConfig.class)`)."],
      ["What does `javax` → `jakarta` mean for this app's next hop?", "Boot 3 needs the Jakarta EE 9+ package names (Day 11). Boot 2.7 still uses `javax`, so today's move doesn't change imports."],
    ],
    done: "`./gradlew build` is green with {{solution.tests}} tests and no XML under `src/main`, `./gradlew bootRun` serves `GET /accounts`, and the Day 30 collection passes {{bruno.requests}}/{{bruno.requests}}.",
    terms: ["WAR", "application server", "web.xml", "embedded server", "@ImportResource"],
    hints: [
      { task: "A Boot build", hints: [
        "Boot comes in as Gradle plugins; then starters replace the hand-picked Spring jars.",
        "Day 6's `build.gradle.kts` is a Boot 2.7.18 build. Compare its `plugins` and `dependencies` with today's.",
        "Plugins `org.springframework.boot` 2.7.18 and `io.spring.dependency-management`; `spring-boot-starter-web` replaces `spring-webmvc`, Jackson and the Servlet API. Then run `dependencyInsight --dependency spring-webmvc` and compare with 5.3.39.",
      ] },
      { task: "From XML to Java", hints: [
        "Read each XML element and ask what Boot already does for you.",
        "The brief's \"What's in the starter\" table, and Boot 2.7's \"Importing XML Configuration\" section.",
        "`AccountsApplication` with `@SpringBootApplication` in `com.example.accounts` (so the scan covers `controller`); a `@Configuration` class with `@Bean AccountService accountService(@Value(\"${accounts.currency}\") String currency)`; `accounts.currency` moves to `application.properties`; delete `src/main/webapp`.",
      ] },
      { task: "The old tests fail now", hints: [
        "`AccountControllerTest` still points at XML files you deleted.",
        "Day 5's `@WebMvcTest` and Day 14's `@Import`.",
        "`@WebMvcTest(AccountController.class)` + `@Import(<your config class>)` + `@Autowired MockMvc`; the test methods stay the same.",
      ] },
    ],
  },
};
