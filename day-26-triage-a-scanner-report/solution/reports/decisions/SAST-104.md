# Decision record: SAST-104

| Field | Value |
|---|---|
| Finding id | SAST-104 |
| Rule | `java/hardcoded-credential` |
| Location | `src/test/java/com/example/accounts/config/PasswordStorageTest.java:39` |
| Decision | False positive |
| Justification | Test code under `src/test`, which is never packaged into the application jar. `ada-pass` is the documented password of a local demo user and is used only to check that the password encoder hashes and matches. It opens nothing real. Suggest the security team excludes `src/test` from this rule. |
| Owner | Accounts API tech lead |
| Review date | 2027-11-10 |
