# Triage: Example SAST scan of accounts-api (main, 2026-11-10)

The security team ran the organisation's SAST scanner on our `main` branch and sent us
`reports/sast-report.sarif`. Every finding needs a decision before the release.

Decision is one of: **Fix** (fixed in code, in this change), **False positive** (the code is safe),
**Accept risk** (real, but we live with it for now), **Duplicate** (the same problem as another finding).
Every False positive and Accept risk also needs a record in `reports/decisions/<finding id>.md`
made from `reports/RISK-ACCEPTANCE-TEMPLATE.md`.

| Finding | Rule | Location | Decision | Justification |
|---|---|---|---|---|
| SAST-101 | `java/hardcoded-credential` | `NotificationsClient.java:10` | Fix | A real, live key in the code. It now comes from `notifications.api-key` (env `NOTIFICATIONS_API_KEY`). The old key is in git history, so the notifications team revoked it and issued a new one. Proved by `NoHardcodedSecretsTest`. |
| SAST-102 | `java/weak-hash-algorithm` | `TokenHasher.java:16` | Fix | MD5 is broken. Tokens are long random values, so SHA-256 is the right replacement (passwords would need a slow, salted encoder instead: Day 17). Proved by `TokenHashingTest`. Existing MD5 hashes must be re-issued, as tokens can't be re-hashed without the token. |
| SAST-103 | `java/sql-injection` | `AccountService.java:51` | False positive | A Spring Data derived query: the query text is fixed, and `username` and `currency` are bound parameters. Proved by `AccountSearchTest.aQuoteInTheCurrencyCannotWidenTheSearch` (Day 16). Record: `reports/decisions/SAST-103.md`. |
| SAST-104 | `java/hardcoded-credential` | `PasswordStorageTest.java:39` | False positive | Test code, never shipped. `ada-pass` is a published demo password used to check the encoder, not a credential for any real system. Record: `reports/decisions/SAST-104.md`. |
| SAST-105 | `config/h2-console-enabled` | `application.properties:14` | Accept risk | Real: the console gives full database access. Today the service only runs on developer laptops with demo data, and the console needs a login. Move it to a `local` profile before the first shared deployment. Record: `reports/decisions/SAST-105.md`. |
| SAST-106 | `secrets/generic-api-key` | `NotificationsClient.java:10` | Duplicate | Same key on the same line as SAST-101, found by a second rule. Fixed by the SAST-101 change. |
