# Mission: Java & Spring Boot for developers joining a team

## Why
Developers new to Java are joining a team (with colleagues who don't write code, such as analysts, testers and risk and security staff, following along) whose services span modern **and older** Java/Spring Boot versions. They need to safely change those services, and in particular to **remediate security findings** and **modernise ageing platforms** (old JDKs and Boot lines past end of support).

## Success looks like
Starting from the team's "Build an Onboarding Tracker REST API" workshop (Day 0: a Boot project with an entity, a repository, POST/GET, validation, tested with Bruno), developers can:
- Explain the core Java in what they built (records, `LocalDate`, `BigDecimal`, `Optional`), not just run it (Day 0 afternoon, `reference/java-idioms.md`)
- Finish, debug, refactor and test a Spring Boot REST API: full CRUD, problem-detail errors, unit and slice tests (Days 1–5)
- Get an older Java 11 / Spring Boot 2.7 service building and running, and audit it (Day 6)
- Read and migrate the JUnit 4 tests older services still carry (Day 7)
- Take a dependency-scan finding (a CVE), trace the vulnerable library, and ship a tested version bump, or record a decision (Days 8–9)
- Fix common Spring Boot security misconfigurations: exposed actuator endpoints, the H2 console, committed secrets (Day 10)
- Upgrade a service's Spring Boot line (2.7 → 3.5) and JDK (11 → 17 → 21), inspecting JDK, Gradle and dependency versions first, and re-scanning after (Days 11–13)
- Recognise and fix the OWASP Top 10:2025 risks most common in an API that handles money: broken access control (IDOR), injection, weak password storage, missing security logging, half-finished transactions (Days 14–19)
- Handle the problems that make payments code different: concurrent updates, retried payments (idempotency), currency and decimal places, and masking account numbers (Days 20–23)
- Work across the wider estate: Maven builds, a Java 8 service, a scanner report triaged with recorded risk decisions, a zero-day drill across several services, container base images, Spring Boot 3.5 → 4 on Java 25, and a pre-Spring Boot application (Days 24–30)
- Connect each day to the team's own services ("In our code" in every afternoon session)

## Constraints
- **One day = one question and one answer.** A morning delivery of the task (30 minutes at most), work through the day with an ongoing chat for help, and a 20–30 minute afternoon walkthrough of the solution that sets up the next day.
- Each day is a folder: `brief.docx` (the question, in the style of the team's workshop briefs), runnable `starter/` and `solution/` (Gradle or Maven wrapper, no global installs), `bruno/` checks where the day calls the API, and `afternoon-walkthrough.md` (presenter notes).
- Don't re-teach what the Onboarding Tracker workshop (Day 0) covers.
- Learners are supported without losing the one-question shape: a two-minute recall and a definition of done in every brief, graded hints released in the chat, a glossary, a troubleshooting sheet and a setup check (`tools/doctor.sh`).
- Stick with Spring Boot; the running example is the **Accounts API**. Gradle throughout, except the Maven day; Days 25, 27 and 30 are separate projects from the wider estate.
- Every day opens with an **In plain English** summary for non-developers.

## Out of scope
- Writing exploits; we *detect and remediate* known vulnerabilities only
- Cloud deployment and Kubernetes (Day 28 covers only the container image)
