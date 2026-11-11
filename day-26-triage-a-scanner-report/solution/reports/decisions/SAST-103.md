# Decision record: SAST-103

| Field | Value |
|---|---|
| Finding id | SAST-103 |
| Rule | `java/sql-injection` |
| Location | `src/main/java/com/example/accounts/service/AccountService.java:51` |
| Decision | False positive |
| Justification | `findByUsernameAndCurrency` is a Spring Data derived query: Spring Data writes a fixed query from the method name and sends `username` and `currency` as bound parameters, never as query text. The test `AccountSearchTest.aQuoteInTheCurrencyCannotWidenTheSearch` sends `GBP' OR '1'='1` and gets no extra rows. Reference: https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html |
| Owner | Accounts API tech lead |
| Review date | 2027-11-10 |
