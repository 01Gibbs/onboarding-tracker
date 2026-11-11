# OWASP Top 10:2025: cheat sheet

The compressed version of the security days (8–12 and 14–19). Source: [OWASP Top 10:2025](https://top10.owasp.org/2025/). Print it, pin it, use it in code review.

| # | Risk | In one line | How it looked in our code | The fix | Day |
|---|---|---|---|---|---|
| A01 | **Broken Access Control** | Users can do or see things they shouldn't | Any logged-in user could `DELETE`; Ada could read account 2 by changing the URL (IDOR) | Deny by default; role rules; **check ownership in the service**; 404 for others' records | [14](../day-14-login-and-roles/afternoon-walkthrough.md), [15](../day-15-whose-account-is-it/afternoon-walkthrough.md) |
| A02 | **Security Misconfiguration** | Safe code, unsafe settings | Every actuator endpoint public; H2 console on; a secret in `application.properties` | Expose only `health`/`info`; dev-only profiles; secrets from the environment | [10](../day-10-security-misconfigurations/afternoon-walkthrough.md) |
| A03 | **Software Supply Chain Failures** | Vulnerable or untrusted libraries | Boot 2.7's transitive Tomcat, SnakeYAML, H2… | Lockfile, scan, patch-bump overrides, upgrade the line | [8](../day-08-what-do-we-ship/afternoon-walkthrough.md), [9](../day-09-patch-and-triage/afternoon-walkthrough.md), [11](../day-11-boot3-migration/afternoon-walkthrough.md), [12](../day-12-rescan-after-migration/afternoon-walkthrough.md) |
| A04 | **Cryptographic Failures** | Weak or missing crypto | `{noop}` plain-text passwords | Salted, slow hashes (PBKDF2, Argon2, scrypt) via `DelegatingPasswordEncoder` | [17](../day-17-storing-passwords/afternoon-walkthrough.md) |
| A05 | **Injection** | Input becomes part of a command | JPQL built with `+`: `GBP' OR '1'='1` returned every account | Bound parameters: query methods, `:named`, `setParameter`. Never `+`. | [16](../day-16-injection/afternoon-walkthrough.md) |
| A06 | Insecure Design | Missing controls by design | (not covered) | Threat modelling, secure design patterns | — |
| A07 | **Authentication Failures** | Weak logins | Demo users with default passwords; unlimited guesses | No default credentials; hashed passwords; throttle and log failures | [14](../day-14-login-and-roles/afternoon-walkthrough.md), [17](../day-17-storing-passwords/afternoon-walkthrough.md), [18](../day-18-audit-logs/afternoon-walkthrough.md) |
| A08 | Software or Data Integrity Failures | Unverified code or data | (not covered) | Signed artifacts, verified updates | — |
| A09 | **Security Logging and Alerting Failures** | Attacks nobody sees | Failed logins and refused lookups left no trace | Audit log who/what/why; never passwords; clean line breaks | [18](../day-18-audit-logs/afternoon-walkthrough.md) |
| A10 | **Mishandling of Exceptional Conditions** | Errors leave things broken or leak detail | A failed transfer debited Ada and credited nobody | `@Transactional` (all or nothing); check before you change; generic error bodies | [1](../day-01-complete-the-crud-api/afternoon-walkthrough.md), [19](../day-19-all-or-nothing-transfers/afternoon-walkthrough.md) |

## Code review questions

- **Who may call this, and whose data does it touch?** Look for an ownership check, not just a role (A01).
- **Is any query, command or log line built with `+` from input?** (A05, A09)
- **Where do passwords and secrets live, and how are they stored?** (A02, A04, A07)
- **If the second step fails, what state is left behind?** Look for `@Transactional` (A10).
- **Would we notice this attack in the logs?** (A09)
- **Which libraries does this add, and are they current?** (A03)

## Snippets

```java
// A01: deny by default, specific rules first
.requestMatchers(HttpMethod.GET, "/accounts/{id}").hasAnyRole("CUSTOMER", "TELLER")
.requestMatchers("/accounts/**").hasRole("TELLER")
.anyRequest().authenticated()

// A05: bound parameters
List<Account> findByUsernameAndCurrency(String username, String currency);

// A04: hashes with an algorithm prefix
new DelegatingPasswordEncoder("pbkdf2", Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()));

// A09: who and why, never the password, no line breaks
audit.warn("LOGIN_FAILED user={} reason={}", AuditLog.clean(name), reason);

// A10: all or nothing
@Transactional
public void transfer(...) { /* check both accounts, then change both */ }
```
