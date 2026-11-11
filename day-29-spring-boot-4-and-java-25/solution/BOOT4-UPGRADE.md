# Upgrade log: Boot 3.5.16 / Java 21 / Gradle 8.14.6 → Boot 4.1.2 / Java 25 / Gradle 9.8.2

Source: the [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide). One change at a time, then `./gradlew build`.

| # | Change | What broke | Fix |
|---|---|---|---|
| 1 | Gradle wrapper 8.14.6 → 9.8.2 (copied `gradle/wrapper/` from a modern day) | Nothing: Boot 3.5.16 builds on Gradle 9. Only `PlatformVersionTest` is still red. `--warning-mode all` shows a Gradle 11 deprecation (`Configuration.setVisible`) coming from the Boot 3.5 plugin | None needed; it goes away with the Boot 4 plugin |
| 2 | Boot plugin `3.5.16` → `4.1.2`, toolchain `of(21)` → `of(25)` | `compileJava`: `Resolved 'org.springframework.boot:spring-boot-jdbc:4.1.2' which is not part of the dependency lock state` (and dozens more) | `./gradlew dependencies --write-locks` |
| 3 | (same) | `compileTestJava`: `package org.springframework.boot.test.web.client does not exist` (`TestRestTemplate`), `package org.springframework.boot.test.autoconfigure.web.servlet does not exist` (`WebMvcTest`, `AutoConfigureMockMvc`) | New packages: `org.springframework.boot.resttestclient.TestRestTemplate`, `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest` / `AutoConfigureMockMvc` |
| 4 | New imports only | Still `package org.springframework.boot.webmvc.test.autoconfigure does not exist`: the classes live in jars that `spring-boot-starter-test` no longer brings | Per-technology test starters: `spring-boot-starter-webmvc-test`, `-security-test`, `-actuator-test`; `spring-boot-resttestclient` (test) + `spring-boot-restclient` (test runtime). Rename `spring-boot-starter-web` → `spring-boot-starter-webmvc` |
| 5 | New dependencies | `Resolved 'org.springframework.boot:spring-boot-starter-webmvc:4.1.2' which is not part of the dependency lock state` | `--write-locks` again (after every dependency change) |
| 6 | (build runs) | `H2ConsoleTest`: `No qualifying bean of type 'org.springframework.boot.resttestclient.TestRestTemplate'` | `@AutoConfigureTestRestTemplate` on the test class |
| 7 | Green, but… | `dependencyInsight --dependency tomcat-embed-core` shows **Tomcat 10.1.61**: Day 12's `extra["tomcat.version"]` override now *downgrades* Boot 4's Tomcat 11 | Delete the override (Boot 4.1.2 manages Tomcat 11.0.27), `--write-locks` |
| 8 | Green, but… | `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`: `/h2-console/` is **404**. The console moved to its own module, so `spring.h2.console.enabled=true` silently does nothing. `H2ConsoleTest` still passes, for the wrong reason | Add `spring-boot-h2console`, `--write-locks`; dev console is 200 again |
| 9 | Delete `gradle/gradle-daemon-jvm.properties` | Nothing: Gradle 9.8.2 runs on JDK 25 itself | Settings comment updated; the foojay resolver stays so a machine without JDK 25 can download it |

## Checked, no change needed

- `@MockBean`: already `@MockitoBean` (Day 11).
- Jackson 3: no `com.fasterxml.jackson` imports in our code. Only visible change: error-body `timestamp` is `…Z` instead of `…+00:00`.
- Spring Security 7: `SecurityConfig` compiles and every security test passes unchanged. Observed: `WWW-Authenticate: Basic realm="Realm", charset="UTF-8"`.
- Actuator: `/actuator/health` now lists `"groups":["liveness","readiness"]` (probes on by default). Bruno's `status: UP` check still passes.
- H2 2.3.232 → 2.4.240: a database file written by the starter opens in the solution.
