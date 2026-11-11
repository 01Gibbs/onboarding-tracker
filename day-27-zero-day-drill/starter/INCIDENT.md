# INC-2026-1111: Critical vulnerability in Apache Log4j (CVE-2021-44228, "Log4Shell")

| | |
|---|---|
| Raised | 2026-11-11 09:00 by the security team |
| Severity | Critical (CVSS 10.0) |
| Advisory | https://nvd.nist.gov/vuln/detail/CVE-2021-44228 and https://logging.apache.org/security.html |
| Ask | Within 4 hours: say which of our services are affected, patch them, and attach evidence. |

## 1. Affected services

For each service: does it ship `log4j-core`? Which version? Through which dependency?

| Service | Ships log4j-core? | Version | How it gets there (dependency path) | Affected? |
|---|---|---|---|---|
| payments | | | | |
| statements | | | | |
| notifications | | | | |

## 2. How we found out

The exact command(s) you ran, and what they showed.

```
(paste here)
```

## 3. The fix

- The release you picked, and why (which of the December 2021 CVEs it fixes, with the sources):
- What you changed (file and line):

## 4. Evidence

- Tests that prove each affected service now ships a fixed version (names, and the result of `./gradlew test`):
- The dependency check, run again after the fix:

## 5. Anything else the security team should know

(Services not affected, and why. Anything you couldn't check.)
