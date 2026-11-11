# Day 18 afternoon — Log Security Events

> **In plain English:** We make the service write down security-relevant events, such as failed logins and refused requests, so attacks can be spotted and investigated.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`security.audit` log** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-18-audit-logs/starter day-18-audit-logs/solution
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

1. **`SecurityAuditListener`**: `@EventListener` on `AbstractAuthenticationFailureEvent` → `LOGIN_FAILED user=… reason=…` on logger `security.audit`.
2. **`AuditLog.clean`**: Replaces `\r`/`\n`: no forged log lines.
3. **`ACCESS_DENIED` in `getVisibleTo`**: OWASP A01: log access-control failures.
4. **`SecurityAuditTest`**: Real `httpBasic("ada", "wrong-pass")` + `OutputCaptureExtension`.

## Points to land

- Log who and why; never the attempted password (it's often a real one with a typo).
- Log injection is Day 16 again: untrusted input reaching whoever reads the log.

## Common mistakes

- Logging the whole `Authentication` object or request.
- A test that only checks a line was logged, not that the password wasn't.

## Questions for the room

- **Would you log successful logins with HTTP Basic?**
  <details><summary>Answer</summary>Every request is a login with Basic, so it's noise. With sessions or tokens, yes.</details>

## In our code (2 minutes)

Prepare before the session: find one of our services' logs: find a failed-login line, and anything in it that shouldn't be there. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Transfers that can fail halfway.
