# Java & Spring Boot: Daily Workshops

<!-- course:count -->31<!-- /course:count --> daily workshops for developers who are new to Java and joining a team that looks after Spring Boot services, from current releases to ones years past end of support. The focus is the work that team does: changing services safely, **remediating security findings**, and **modernising old platforms**.

Each day is one question in the morning and its answer in the afternoon. **Start with [HOW-TO-USE.md](HOW-TO-USE.md).** Don't write code? Start with [reference/start-here.md](reference/start-here.md).

## The days

<!-- course:days -->
| Day | Workshop | You'll practise | Builds on |
|---|---|---|---|
| 0 | [Build an Onboarding Tracker REST API](day-00-onboarding-tracker) | POST/GET, validation, H2, tested with Bruno | — |
| 1 | [Complete the Accounts CRUD API](day-01-complete-the-crud-api) | `PUT`, `DELETE`, 404s, problem-detail errors | Day 0's level |
| 2 | [Code Review: Six Bugs](day-02-six-bugs) | a rushed pull request, symptom by symptom | Day 1 |
| 3 | [Refactor Without Changing Behaviour](day-03-refactoring) | constructor injection, layers, a response DTO | Day 2 |
| 4 | [Unit Tests with JUnit 5, AssertJ and Mockito](day-04-unit-tests) | JUnit 5, AssertJ, Mockito | Day 3 |
| 5 | [Testing the Web Layer and the Repository](day-05-slice-tests) | `@WebMvcTest`, `@DataJpaTest` | Day 4 |
| 6 | [Get a 2022 Service Running, and Audit It](day-06-a-legacy-service) | get Java 11 / Boot 2.7 running, and audit it | A new (2022) codebase |
| 7 | [Read, Write and Migrate JUnit 4 Tests](day-07-junit4) | read, write and migrate its tests | Day 6 |
| 8 | [What Do We Ship? Lockfile, Scan, Trace](day-08-what-do-we-ship) | lockfile, OSV scan, trace the CVEs (OWASP A03) | Day 7 |
| 9 | [Patch What You Can, Decide the Rest](day-09-patch-and-triage) | version overrides and recorded decisions | Day 8 |
| 10 | [Fix the Security Misconfigurations](day-10-security-misconfigurations) | actuator, H2 console, secrets (OWASP A02) | Day 9 |
| 11 | [Upgrade to Spring Boot 3.5 and Java 17](day-11-boot3-migration) | OpenRewrite, and what it misses | Day 10 |
| 12 | [Re-scan After the Upgrade](day-12-rescan-after-migration) | upgraded isn't done | Day 11 |
| 13 | [Upgrade the JDK from 17 to 21](day-13-jdk-21) | inspect first, then upgrade | Day 12 |
| 14 | [Login and Roles for the Modern API](day-14-login-and-roles) | Spring Security, deny by default, roles (OWASP A01) | Day 5 |
| 15 | [Whose Account Is It? Fixing an IDOR](day-15-whose-account-is-it) | an IDOR and ownership checks (OWASP A01) | Day 14 |
| 16 | [Find and Fix an Injection](day-16-injection) | string-built JPQL and bound parameters (OWASP A05) | Day 15 |
| 17 | [Store Passwords Properly](day-17-storing-passwords) | salted, slow hashes (OWASP A04, A07) | Day 16 |
| 18 | [Log Security Events](day-18-audit-logs) | failed logins and refused lookups (OWASP A09) | Day 17 |
| 19 | [All or Nothing: Safe Transfers](day-19-all-or-nothing-transfers) | `@Transactional`, failing closed (OWASP A10) | Day 18 |
| 20 | [Two Transfers at Once](day-20-two-transfers-at-once) | lost updates and optimistic locking | Day 19 |
| 21 | [Idempotent Payments](day-21-idempotent-payments) | a retry must never pay twice | Day 20 |
| 22 | [Money Done Right](day-22-money-done-right) | currencies and decimal places | Day 21 |
| 23 | [Mask What You Show](day-23-mask-what-you-show) | account numbers in responses and logs | Day 22 |
| 24 | [Maven for Gradle Users](day-24-maven) | `./mvnw dependency:tree`, a `<properties>` version override | Day 5's API, built with Maven |
| 25 | [From Java 8 to 11](day-25-java-8-to-11) | move a Java 8 service to 11, and add JAXB back as a library | A Java 8 service |
| 26 | [Triage a Scanner Report](day-26-triage-a-scanner-report) | a SARIF report: fix, false positive, accept risk, duplicate | Day 19 |
| 27 | [Zero-Day Drill](day-27-zero-day-drill) | Log4Shell across a multi-project build: `dependencyInsight`, one version, evidence | A new multi-project Gradle build: three services and two shared libraries |
| 28 | [Patch the Container, Not Just the Code](day-28-container-base-images) | a supported, matching, non-root base image | Day 13 |
| 29 | [The Next Hop: Spring Boot 4 on Java 25](day-29-spring-boot-4-and-java-25) | Boot 3.5 → 4.1, Java 21 → 25, Gradle 8.14 → 9.8 | Day 13 |
| 30 | [Move a Pre-Boot Spring App onto Spring Boot](day-30-pre-boot-spring) | XML beans, `web.xml` and a WAR → Spring Boot 2.7 with embedded Tomcat | A new pre-Boot Spring MVC codebase (WAR) |
<!-- /course:days -->

<!-- course:modern-days -->Days 0–5, 14–24, 26<!-- /course:modern-days --> use the current stack (Java 25, Spring Boot 4.1.2). <!-- course:legacy-days -->Days 6–13, 28–29<!-- /course:legacy-days --> use the same Accounts API as written in 2022 (Java 11, Spring Boot 2.7.18), which learners patch, harden and upgrade to Spring Boot 3.5 on Java 21, then to Spring Boot 4 on Java 25. <!-- course:estate-days -->Days 25, 27, 30<!-- /course:estate-days --> are separate projects from the wider estate.

## In this repository

| Folder | What's in it |
|---|---|
| `day-NN-topic/` | One day: brief, starter, solution, Bruno checks, hints, afternoon walkthrough |
| `reference/` | Glossary, troubleshooting, Java idioms, the app diagram, cheat sheets, using Bruno |
| `tools/` | `doctor.sh` (setup check), the course verifier and the brief generators |
| `docs/` | Why the course exists, its sources, maintainer notes and design history |
