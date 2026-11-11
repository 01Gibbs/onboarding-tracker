# Dependency findings: Accounts API (now Boot 3.5.16, Java 17)

| Scan | Vulnerable libraries | Advisories | CRITICAL | HIGH |
|---|---|---|---|---|
| Before (Day 8) | 22 | 123 | 14 | 49 |
| After overrides (Day 9) | 18 | 62 | 3 | 21 |

Scanned with `java ../../tools/OsvCheck.java` on 2026-11-10. Your numbers will differ: new advisories land every week.

## Decisions

| Library | Was | Now | Decision | Why |
|---|---|---|---|---|
| `com.h2database:h2` | 1.4.200 | 2.1.214 | ✅ **Fixed**: removed our pin, so Boot picks its managed version | The console RCEs are gone. One HIGH remains, CVE-2022-45868, which the H2 maintainers **dispute**. ⚠️ **Accepted risk** (updated in Day 10): the console is now off outside the `dev` profile and never accepts remote connections, so the affected feature isn't reachable in production. `H2ConsoleTest` guards this |
| `org.yaml:snakeyaml` | 1.30 | 2.2 | ✅ **Fixed**: `extra["snakeyaml.version"]` | Boot 2.7.10+ supports SnakeYAML 2.x. Boot says it wasn't exploitable, but a clean scan is worth a one-line override |
| `ch.qos.logback:logback-*` | 1.2.12 | 1.2.13 | ✅ **Partly fixed**: `extra["logback.version"]` | 1.2.13 is the last 1.2.x. The remaining advisories are fixed only in 1.3+/1.5+, which Boot 2.7 doesn't support → **needs Boot 3** |
| `org.apache.tomcat.embed:*` | 9.0.83 | 9.0.123 | ✅ **Fixed**: `extra["tomcat.version"]` | A patch bump within Tomcat 9.0.x. Low risk, and the tests pass |
| `org.springframework.security:*` | 5.7.11 | 5.7.13 | ✅ **Partly fixed**: `extra["spring-security.version"]` | Last open-source 5.7.x. The CRITICAL CVE-2026-22732 is not fixed in any open-source 5.7.x → **needs Boot 3** |
| `org.springframework:*` | 5.3.31 | 5.3.39 | ✅ **Partly fixed**: `extra["spring-framework.version"]` | Last open-source 5.3.x. CVE-2016-1000027 (CRITICAL) is only fixed in Framework 6 → **needs Boot 3** |
| `com.fasterxml.jackson.core:*` | 2.13.5 | 2.13.5 | ⏸ **Deferred** | Jumping Jackson several minor versions under Boot 2.7 is risky. Boot 3.5 manages a current version → **needs Boot 3** |
| `org.springframework.boot:*` | 2.7.18 | 2.7.18 | ⛔ **Blocked** | e.g. CVE-2025-22235 is fixed only in commercial 2.7.x builds → **needs Boot 3** |

## Day 11 update: after the migration

Every "needs Boot 3" finding above is **✅ Fixed** by the migration: Spring Framework 6.2, Spring Security 6.5, Logback 1.5, current Jackson and Tomcat 10.1. All five `extra[...]` overrides were deleted, because Boot 3.5 manages newer versions itself. H2 is now 2.3.232, which has no open advisories, so the Day 9 accepted risk is closed.

## Conclusion

Patch-level overrides removed every CRITICAL in Tomcat and H2, and halved the advisory count. The three CRITICALs left were in Spring Framework/Security 5.x, which have **no open-source fix**. The migration (Day 11) replaced all of them. Next step: re-scan on Boot 3.5.16 (Day 12).
