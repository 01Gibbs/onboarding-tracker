# Decision record: SAST-105

| Field | Value |
|---|---|
| Finding id | SAST-105 |
| Rule | `config/h2-console-enabled` |
| Location | `src/main/resources/application.properties:14` |
| Decision | Accept risk |
| Justification | Real: the H2 console gives anyone who reaches it full read and write access to the database. Today the risk is low: the service only runs on developer laptops with demo data, the console sits behind the API's login (`anyRequest().authenticated()`), and there is no shared or production deployment. Plan: move `spring.h2.console.enabled=true` to an `application-local.properties` profile before the first shared deployment (ticket ACC-412). If a shared deployment is planned before then, this acceptance ends and the fix is required. |
| Owner | Accounts API tech lead |
| Review date | 2027-02-10 |
