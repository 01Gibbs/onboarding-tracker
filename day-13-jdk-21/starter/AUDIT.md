# Legacy audit: Accounts API

## The platform

| Question | Answer |
|---|---|
| Java version the code is compiled for (and where that's set) | **11**: `java { toolchain { languageVersion.set(JavaLanguageVersion.of(11)) } }` in `build.gradle.kts` |
| Spring Boot version (and where that's set) | **2.7.18**: the `org.springframework.boot` plugin version in `build.gradle.kts` |
| Open-source support for this Boot line ended on | **2023-06-30**. 2.7.18 (Nov 2023) was the last open-source release; later fixes are commercial-only |
| Gradle version (and where that's set) | **8.14.6**: `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties`. Boot 2.7 can't build on Gradle 9 |
| Which JDK runs Gradle itself, and why it isn't your JDK 25 | **17**, from `gradle/gradle-daemon-jvm.properties`. Gradle 8.14 can't run on Java 25 (that needs Gradle 9.1+), so Gradle downloads a JDK 17 for itself |

## Legacy markers

1. `javax.persistence.*` imports in `model/Account.java` (Boot 3 uses `jakarta.persistence`)
2. `javax.validation.*` imports in `dto/CreateAccountRequest.java` and `controller/AccountController.java`
3. `config/SecurityConfig.java` extends `WebSecurityConfigurerAdapter`, which was removed in Spring Security 6
4. `antMatchers(...)` and `authorizeRequests()` in `config/SecurityConfig.java` (now `requestMatchers` / `authorizeHttpRequests`)
5. DTOs are classes with getters/setters (`dto/*.java`): Java 11 has no records
6. `@MockBean` in `controller/AccountControllerTest.java` (deprecated in Boot 3.x, removed in Boot 4)
7. `spring-boot-starter-web` in `build.gradle.kts` (Boot 4 renamed it `spring-boot-starter-webmvc`)
8. `.collect(Collectors.toList())` in `AccountController` (Java 16+ has `.toList()`)
9. JUnit 4 tests (`org.junit.Test`, `@RunWith`) in `AccountsApplicationTests` and `service/AccountServiceTest`, next to the JUnit 5 `AccountControllerTest`. They need `junit-vintage-engine` in `build.gradle.kts` to run

## What will block an upgrade to Spring Boot 3?

- Java 11: Boot 3 needs **Java 17+**
- Every `javax.*` import must become `jakarta.*`
- `WebSecurityConfigurerAdapter` and `antMatchers` don't exist in Spring Security 6

## Anything that looks risky?

- `extra["h2.version"] = "1.4.200"` pins a very old H2 "for now", since 2021
- `management.endpoints.web.exposure.include=*` and `/actuator/**` is `permitAll()` in `SecurityConfig`
- `management.endpoint.shutdown.enabled=true`
- The H2 console is on, with `web-allow-others=true`
- A live-looking credential (`accounts.fx-provider.api-credential`) is committed in `application.properties`
