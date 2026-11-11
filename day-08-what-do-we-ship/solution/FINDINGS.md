# Dependency findings: Accounts API (Boot 2.7.18)

Scanned `gradle.lockfile` with `java ../../tools/OsvCheck.java` on 2026-11-10.
New advisories are published every week, so **your numbers will differ**. That's normal.

**Totals:** 73 runtime libraries · 22 vulnerable · 123 advisories (CRITICAL 14 · HIGH 49 · MODERATE 45 · LOW 15)

## Top findings

| Library | Version | Worst | # | How it gets here (`dependencyInsight`) | Notes |
|---|---|---|---|---|---|
| `com.h2database:h2` | 1.4.200 | CRITICAL | 4 | Our `runtimeOnly("com.h2database:h2")`, **pinned** by `extra["h2.version"]` | H2 console RCEs: CVE-2021-42392, CVE-2022-23221. Boot 2.7.18 would pick 2.1.214 if we removed the pin |
| `org.apache.tomcat.embed:tomcat-embed-core` | 9.0.83 | CRITICAL | 40 | `spring-boot-starter-web` → `spring-boot-starter-tomcat` | Managed by Boot. Tomcat 9.0.x still gets patch releases |
| `org.springframework:spring-webmvc` (and other `spring-*` 5.3.31) | 5.3.31 | CRITICAL | 16 | `spring-boot-starter-web` | Spring Framework 5.3.x: open-source support has ended |
| `org.yaml:snakeyaml` | 1.30 | HIGH | 7 | `spring-boot-starter` | CVE-2022-1471. The Boot team says Boot itself isn't exploitable this way, but scanners will flag it until it's upgraded |
| `ch.qos.logback:logback-core` | 1.2.12 | HIGH | 8 | `spring-boot-starter-logging` | CVE-2023-6378 fixed in 1.2.13 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.5 | HIGH | 11 | `spring-boot-starter-json` | Jackson 2.13 is old; a big jump is risky on Boot 2.7 |
| `org.springframework.security:spring-security-*` | 5.7.11 | HIGH | 8 | `spring-boot-starter-security` | CVE-2024-38821 fixed in 5.7.13 |
| `org.springframework.boot:spring-boot*` | 2.7.18 | HIGH | 4 | the Boot plugin itself | e.g. CVE-2025-22235: fixed only in **commercial** 2.7.x releases |

## First impressions

- Almost everything arrives **transitively**, through a Spring Boot starter. We declared only 6 runtime dependencies.
- The one we did touch (the H2 pin) is one of the worst.
- Some fixes are a **patch bump within the same line**. Others don't exist for Boot 2.7 at all.
