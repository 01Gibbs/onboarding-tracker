# Day 14 afternoon — Login and Roles for the Modern API

> **In plain English:** We add logins to the modern service, so only known people get in, and each person can only do what their role allows.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Security filter chain** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-14-login-and-roles/starter day-14-login-and-roles/solution
```

## Agenda (25 min)

| Time | What |
|---|---|
| 0–3 | Recap the question. Show of hands: who got it green? Where did people get stuck in the chat today? |
| 3–14 | Solution tour (below), in order |
| 14–19 | Points to land, and common mistakes |
| 19–22 | Questions for the room |
| 22–25 | In our code, then tomorrow's question |

## Solution tour

1. **`build.gradle.kts`**: `spring-boot-starter-security` (+ `-security-test`). Alone, it locks everything and prints a generated password.
2. **`SecurityConfig`**: `/hello` public; `GET /accounts/{id}` CUSTOMER or TELLER; `/accounts/**` TELLER; `anyRequest().authenticated()`. First match wins. `httpBasic`, `STATELESS`, CSRF off.
3. **`InMemoryUserDetailsManager`**: ada, grace, teller with `{noop}` passwords: Day 17 fixes them.
4. **`AccountControllerTest`**: `@Import(SecurityConfig.class)` (otherwise the slice uses Boot's defaults: POSTs get 403 from CSRF), `@WithMockUser(roles = "TELLER")`, `@WithAnonymousUser`.

## Points to land

- 401 = who are you? 403 = you can't do that.
- Deny by default: anything not listed needs a login.
- CSRF off is only safe because no browser holds a session for this API.
- OWASP A01:2025 Broken Access Control is #1.

## Common mistakes

- General rules before specific ones (first match wins).
- Forgetting `@Import`: tests pass against the wrong rules.

## Questions for the room

- **Can a customer now read someone else's account?**
  <details><summary>Answer</summary>Yes, by changing the id. Tomorrow.</details>

## In our code (2 minutes)

Prepare before the session: find one of our `SecurityFilterChain`s. Is it deny by default? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Stop customers reading each other's accounts: an IDOR.
