# INC-2026-1111: Critical vulnerability in Apache Log4j (CVE-2021-44228, "Log4Shell")

| | |
|---|---|
| Raised | 2026-11-11 09:00 by the security team |
| Severity | Critical (CVSS 10.0) |
| Advisory | https://nvd.nist.gov/vuln/detail/CVE-2021-44228 and https://logging.apache.org/security.html |
| Ask | Within 4 hours: say which of our services are affected, patch them, and attach evidence. |

## 1. Affected services

| Service | Ships log4j-core? | Version | How it gets there (dependency path) | Affected? |
|---|---|---|---|---|
| payments | Yes | 2.14.1 | `services:payments` → `libs:audit` → `log4j-core` | **Yes** |
| statements | Yes | 2.14.1 | `services:statements` → `libs:reporting` → `libs:audit` → `log4j-core` (transitive: statements never mentions Log4j) | **Yes** |
| notifications | No | (log4j-api and log4j-to-slf4j 2.14.1 only) | `services:notifications` → `log4j-api`, `log4j-to-slf4j` → logs go to SLF4J | No: only the `log4j-core` jar is affected |

## 2. How we found out

The runtime classpath (what each service ships), one service at a time:

```
for s in payments statements notifications; do
  echo "== $s"
  ./gradlew -q :services:$s:dependencyInsight --dependency log4j-core --configuration runtimeClasspath
done
```

payments and statements showed `org.apache.logging.log4j:log4j-core:2.14.1` with the paths above.
notifications showed "No dependencies matching given input were found".
`--dependency org.apache.logging.log4j` on notifications showed only `log4j-api` and `log4j-to-slf4j`.

## 3. The fix

- Fixed version: the last CVE in the series (CVE-2021-44832) is fixed in 2.17.1 for Java 8 and later (NVD), so 2.17.1 is the floor.
  We picked **2.26.1**, the latest 2.x release on Maven Central (2026-06-29), which also fixes the 2025–2026 Log4j advisories.
- What changed:
  - `libs/audit/build.gradle.kts`: the hard-coded `log4j-core:2.14.1` now uses `libs.log4j.core` from the version catalog.
  - `gradle/libs.versions.toml`: `log4j = "2.26.1"` (was 2.14.1). This also moves notifications' `log4j-api` to 2.26.1.

## 4. Evidence

- `PaymentsLog4jVersionTest.log4jCoreIsPatched` and `StatementsLog4jVersionTest.log4jCoreIsPatched` read the version
  from the `log4j-core` jar each service loads at run time and require 2.17.1 or later. Both were red (2.14.1) and are now green.
  `./gradlew test`: 11 tests, 0 failures.
- `Log4jExposureTest.log4jCoreIsNotOnTheClasspath` proves notifications still doesn't ship log4j-core.
- `dependencyInsight` after the fix: `log4j-core:2.26.1` in payments and statements.

## 5. Anything else the security team should know

- notifications was never affected by CVE-2021-44228: it uses the Log4j API routed to SLF4J, the same set-up Spring Boot uses by default.
- `libs:audit` is shared: any other team's service that depends on it must take the new version too. We told the platform team.
- The services must be rebuilt and redeployed: a fix in the build does nothing until the new jars are running.
