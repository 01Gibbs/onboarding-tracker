# Day 10 afternoon — Fix the Security Misconfigurations

> **In plain English:** We close doors that were left open by mistake: admin pages anyone could reach, and a password written straight into the code.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **configuration around the app** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-10-security-misconfigurations/starter day-10-security-misconfigurations/solution
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

1. **`application.properties`**: `management.endpoints.web.exposure.include=health,info`; shutdown line gone; H2 console lines gone; `accounts.fx-provider.api-credential=${FX_API_CREDENTIAL:}`.
2. **`application-dev.properties`**: The console, for the `dev` profile only.
3. **`SecurityConfig.java`**: `.antMatchers("/actuator/health", "/actuator/info").permitAll()` instead of `/actuator/**`.
4. **`H2ConsoleTest`**: Starts a real server (`RANDOM_PORT`): the console is its own servlet, invisible to `MockMvc`.

## Points to land

- Two layers: don't expose it (exposure) *and* don't let strangers reach it (security rules). Defence in depth.
- Boot 2.7 masks only keys *ending* in `password`, `secret`, `key`, `token`…: `api-credential` leaked in plain text.
- A committed secret must be **rotated**: deleting the line doesn't remove it from Git history.
- OWASP A02:2025 Security Misconfiguration.

## Common mistakes

- Fixing only `SecurityConfig` (endpoints still exposed to anyone who logs in) or only the exposure.
- Calling `/actuator/shutdown` while exploring.

## Questions for the room

- **We disable CSRF in `SecurityConfig`. When is that OK?**
  <details><summary>Answer</summary>When no browser holds a session cookie for the API (stateless, HTTP Basic or tokens). Day 14 comes back to it.</details>

## In our code (2 minutes)

Prepare before the session: find which actuator endpoints one of our services exposes, and to whom. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Move to Boot 3.5 and Java 17 with OpenRewrite, and fix what it misses.
