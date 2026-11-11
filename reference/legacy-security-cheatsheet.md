# Cheat sheet: legacy, security & modernisation

The compressed version of Days 6–13. Print it, pin it, come back to it.

## Versions

| Line | Needs Java | Builds with Gradle | Open-source support |
|---|---|---|---|
| Spring Boot 2.7 (last: 2.7.18) | 8–21 | 6.8 – 8.x (**not 9**) | ended 2023-06-30 |
| Spring Boot 3.5 (last: 3.5.16) | 17–25 | 7.6.4+ / 8.4+ | ended 2026-06-30 |
| Spring Boot 4.x | 17+ | 9 supported | current |

Gradle **8.14 can't run on Java 25** (error: `What went wrong: 25.0.5`). Fix: commit `gradle/gradle-daemon-jvm.properties` (`./gradlew updateDaemonJvm --jvm-version=17`) plus the foojay resolver plugin.

## Commands

```bash
# What do we ship?
./gradlew -q dependencies --configuration runtimeClasspath

# Why is this version here?
./gradlew -q dependencyInsight --dependency <name> --configuration runtimeClasspath

# Lock exact versions (needs dependencyLocking { lockAllConfigurations() })
./gradlew dependencies --write-locks

# Scan the lock
osv-scanner scan source -L gradle.lockfile
java ../../tools/OsvCheck.java gradle.lockfile        # no-install alternative

# Automated migration (plugin org.openrewrite.rewrite 7.39.0, BOM 3.37.0)
# Needs org.gradle.jvmargs=-Xmx2g in gradle.properties, or it can run out of memory
./gradlew rewriteDryRun        # writes build/reports/rewrite/rewrite.patch
./gradlew rewriteRun           # applies it
```

## The inspection pattern (before and after any upgrade)

```bash
java -version                                   # the JDK on your PATH
./gradlew -version                              # Gradle version + "Daemon JVM": what runs Gradle
grep -n -A2 toolchain build.gradle.kts          # what compiles and tests your code
./gradlew -q javaToolchains                     # every JDK Gradle can see or downloaded
./gradlew -q dependencies --configuration runtimeClasspath
./gradlew -q dependencyInsight --dependency <name> --configuration runtimeClasspath
#   "Selected by rule" = a Boot version property; org.gradle.jvm.version Provided/Requested = library's min Java vs your toolchain

# Before a JDK upgrade: does anything use JDK internals?
./gradlew bootJar && mkdir -p build/jdeps && (cd build/jdeps && unzip -qo ../libs/*-SNAPSHOT.jar)
jdeps --jdk-internals --multi-release 21 --ignore-missing-deps --class-path 'build/jdeps/BOOT-INF/lib/*' -R build/jdeps/BOOT-INF/classes
```

JDK-only upgrade (Day 13): change the toolchain, leave the daemon JDK alone, check the lockfile didn't change, look for the `using Java 21` log line, then update the `Dockerfile` base image and CI JDK. Java 21 warns when Mockito self-attaches its agent (JEP 451).

## Overriding a Boot-managed version

```kotlin
// With the io.spring.dependency-management plugin. Always say why, and when to remove it.
extra["snakeyaml.version"] = "2.2"     // CVE-2022-1471. Remove when Boot manages >= 2.2
```

- Property names: Boot's "Dependency versions" appendix.
- Gradle `constraints { }` are **ignored** while the dependency-management plugin is applied. Pick one mechanism.
- After any change: `--write-locks`, then `./gradlew build`.
- **Remove all overrides when you change Boot line.** Migration tools don't know about them.

## Triage vocabulary

| Decision | Means |
|---|---|
| ✅ Fixed | Advisory gone after a change, tests green |
| ✅ Partly fixed | Upgraded as far as this line allows |
| ⏸ Deferred | Fixable, but the risk of fixing now outweighs the risk of waiting. Say until when |
| ⚠️ Accepted risk | Not exploitable here, or disputed, and the reason is written down |
| ⛔ Blocked | No fix exists on this line: needs a migration |

*Flagged ≠ exploitable*, but a clean scan is still worth a cheap fix.

## Audit checklist: Spring Boot configuration

- [ ] `management.endpoints.web.exposure.include` lists only what's needed (e.g. `health,info`), never `*`
- [ ] No `permitAll()` on `/actuator/**`
- [ ] `shutdown` endpoint not enabled
- [ ] `spring.h2.console.enabled=true` only in a dev profile; `web-allow-others` never `true`
- [ ] No secrets in `application*.properties`; use `${ENV_VAR:}`. Rotate anything that was committed
- [ ] DevTools not shipped to production (it enables the H2 console)

## JUnit 4 → JUnit 5

| JUnit 4 | JUnit 5 |
|---|---|
| `org.junit.Test`, `public` class and methods | `org.junit.jupiter.api.Test`, package-private is fine |
| `@Before` / `@After` / `@BeforeClass` / `@AfterClass` | `@BeforeEach` / `@AfterEach` / `@BeforeAll` / `@AfterAll` |
| `@RunWith(SpringRunner.class)` | nothing: `@SpringBootTest` already includes it |
| `@RunWith(MockitoJUnitRunner.class)` | `@ExtendWith(MockitoExtension.class)` |
| `@Test(expected = X.class)`, `@Rule ExpectedException` | `assertThrows(...)` or AssertJ `assertThatThrownBy(...)` |
| `Assert.assertEquals(expected, actual)` | `assertThat(actual).isEqualTo(expected)` |

JUnit 4 tests run on the JUnit Platform only with `junit-vintage-engine` (not in `spring-boot-starter-test` since Boot 2.4). **Count tests before and after a migration:** without the vintage engine, JUnit 4 tests can compile and silently never run. OpenRewrite recipe for Spring apps: `org.openrewrite.java.spring.boot2.SpringBoot2JUnit4to5Migration`.

## 2.7 → 3.x: what breaks

| Before | After |
|---|---|
| Java 8/11 | Java 17+ |
| `javax.persistence` / `javax.validation` / `javax.servlet` | `jakarta.*` |
| `extends WebSecurityConfigurerAdapter` | `@Bean SecurityFilterChain` |
| `authorizeRequests().antMatchers(...)` | `authorizeHttpRequests(r -> r.requestMatchers(...))` |
| Error pages not authorized | ERROR dispatches **are** authorized: permit them, or 404s become 401s |
| `@MockBean` | `@MockitoBean` (required in Boot 4) |
| H2 1.4 / 2.1 data files | Incompatible with newer H2: export with `SCRIPT`, re-import with `RUNSCRIPT` |

## Glossary

- **Transitive dependency**: a library you didn't declare, pulled in by one you did.
- **Lockfile**: `gradle.lockfile`, the exact version of every library; the input for scanners.
- **Toolchain JDK**: compiles and tests your code. **Daemon JDK**: runs Gradle itself. Can differ.
- **BOM / dependency management**: the list of tested versions Spring Boot picks for you.
- **CVE / GHSA**: IDs for a published vulnerability (global / GitHub). One issue often has both.
- **Advisory**: a published record of a vulnerability, with affected and fixed versions.
- **OSS support**: free open-source fixes. After it ends, fixes may exist only in paid releases.
- **Profile**: a named set of extra config (`application-dev.properties`), switched on with `SPRING_PROFILES_ACTIVE`.
- **Recipe** (OpenRewrite): an automated, tested source-code refactoring.
