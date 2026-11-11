# Triage: Example SAST scan of accounts-api (main, 2026-11-10)

The security team ran the organisation's SAST scanner on our `main` branch and sent us
`reports/sast-report.sarif`. Every finding needs a decision before the release.

Decision is one of: **Fix** (fixed in code, in this change), **False positive** (the code is safe),
**Accept risk** (real, but we live with it for now), **Duplicate** (the same problem as another finding).
Every False positive and Accept risk also needs a record in `reports/decisions/<finding id>.md`
made from `reports/RISK-ACCEPTANCE-TEMPLATE.md`.

| Finding | Rule | Location | Decision | Justification |
|---|---|---|---|---|
| SAST-101 | `java/hardcoded-credential` | `NotificationsClient.java:10` | _to do_ | |
| SAST-102 | `java/weak-hash-algorithm` | `TokenHasher.java:16` | _to do_ | |
| SAST-103 | `java/sql-injection` | `AccountService.java:51` | _to do_ | |
| SAST-104 | `java/hardcoded-credential` | `PasswordStorageTest.java:39` | _to do_ | |
| SAST-105 | `config/h2-console-enabled` | `application.properties:14` | _to do_ | |
| SAST-106 | `secrets/generic-api-key` | `NotificationsClient.java:10` | _to do_ | |
