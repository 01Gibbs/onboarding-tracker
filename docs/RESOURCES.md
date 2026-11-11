# Java & Spring Boot Resources

Trusted sources behind this course. The daily briefs cite these directly.

## Knowledge

### Versions and support
- [spring.io/projects/spring-boot (Support tab)](https://spring.io/projects/spring-boot)
  Official open-source and commercial support dates for every Boot line. Use for: "is this version still supported?"
- [Adoptium support page](https://adoptium.net/support/)
  How long Temurin builds of each Java LTS are published. Use for: Java end-of-life questions.

### Migration
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
  The canonical 2.7 → 3.x checklist: Java 17, `jakarta`, Security 6, dispatch types, property renames. Use for: Day 11 and any real 2.7 upgrade.
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
  The next hop after 3.5. Use for: moving a 3.5 service onto the Days 1–5 stack.
- [Spring Security: without the WebSecurityConfigurerAdapter](https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter)
  The Spring team's before/after examples. Use for: rewriting old security config by hand.
- [Spring Security 6.5: Authorize HTTP Requests](https://docs.spring.io/spring-security/reference/6.5/servlet/authorization/authorize-http-requests.html)
  `requestMatchers`, dispatcher types, `permitAll`. Use for: any `SecurityFilterChain` question.
- [OpenRewrite: getting started](https://docs.openrewrite.org/running-recipes/getting-started)
  Running recipes from Gradle. Use for: automated migrations. Note the repository change described in the research notes.

### Dependencies and vulnerabilities
- [Spring Boot 2.7 dependency versions appendix](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/dependency-versions.html#appendix.dependency-versions.properties)
  Every managed library and its version **property** name. Use for: `extra["…version"]` overrides.
- [Gradle: dependency locking](https://docs.gradle.org/current/userguide/dependency_locking.html) and [viewing and debugging dependencies](https://docs.gradle.org/current/userguide/viewing_debugging_dependencies.html)
  Lockfiles, `dependencies`, `dependencyInsight`. Use for: "what do we ship, and why this version?"
- [OSV](https://osv.dev) and [OSV-Scanner](https://google.github.io/osv-scanner/)
  Google's open vulnerability database and scanner, with no API key needed. Use for: scanning lockfiles and looking up advisory IDs.
- [OWASP Dependency-Check (Gradle)](https://dependency-check.github.io/DependencyCheck/dependency-check-gradle/index.html)
  What many corporate CI pipelines run. Needs an NVD API key. Use for: understanding CI scan reports.
- [spring.io/security](https://spring.io/security)
  Official Spring advisories with affected and fixed versions. Use for: triaging any Spring CVE.

### Testing frameworks
- [JUnit 5 user guide: Migrating from JUnit 4](https://docs.junit.org/5.8.2/user-guide/#migrating-from-junit4)
  The official mapping of JUnit 4 annotations and assertions to JUnit 5. Use for: Day 7 and any legacy test suite.
- [junit.org/junit4](https://junit.org/junit4/)
  States "JUnit 4 is in maintenance mode". Use for: the business case to migrate.

### Secure coding (OWASP Top 10:2025)
- [OWASP Top 10:2025](https://top10.owasp.org/2025/)
  The 2025 edition. A01 Broken Access Control, A04 Cryptographic Failures, A05 Injection, A07 Authentication Failures, A09 Security Logging and Alerting Failures, A10 Mishandling of Exceptional Conditions are covered in Days 14–19; A02 and A03 in Days 8–10. Use for: naming and explaining a finding.
- [Spring Security reference: Password Storage](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)
  `DelegatingPasswordEncoder`, the `{id}` prefix, PBKDF2/Argon2 encoders. Use for: Day 17.
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)
  Which algorithm and work factor to use. Use for: password hashing decisions.

### Tools
- [Bruno docs](https://docs.usebruno.com/)
  The team's API client. Collections are plain-text `.bru` files that live in Git next to the code. Use for: every day's `bruno/` folder.

### Secure configuration
- [Boot 2.7 Actuator: exposing and securing endpoints](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/actuator.html#actuator.endpoints.exposing)
  Default exposure, and how to secure endpoints. Use for: actuator audit findings.
- [Boot 2.7 data docs: H2 console](https://docs.spring.io/spring-boot/docs/2.7.18/reference/html/data.html#data.sql.h2-web-console)
  "Only intended for use during development". Use for: H2 console findings.
- [OWASP Top 10: A05 Security Misconfiguration](https://owasp.org/Top10/A05_2021-Security_Misconfiguration/) and [A06 Vulnerable and Outdated Components](https://owasp.org/Top10/A06_2021-Vulnerable_and_Outdated_Components/)
  The industry framing for Days 8–10. Use for: explaining *why* to non-engineers.

## Wisdom (Communities)

- **Your team's code review.** Real migration and remediation PRs are where judgement gets built. Ask to review, or pair on, the next one.
- [Stack Overflow `spring-boot` tag](https://stackoverflow.com/questions/tagged/spring-boot)
  Large and well moderated. Use for: specific errors. Search the exact message first.
- [r/java](https://www.reddit.com/r/java/)
  Active Java community. Use for: "how do other teams handle X?" discussions.

## Gaps

- No good primary source compares Gradle-native constraints with the dependency-management plugin. We tested it ourselves; see the research notes, section 6.
- Java 8 toolchain auto-provisioning on Apple Silicon is untested. The legacy days start at Java 11 for that reason.
