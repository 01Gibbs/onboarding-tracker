# Day 17 afternoon — Store Passwords Properly

> **In plain English:** We stop storing passwords in a readable form, and store them the way the security standards require, so a leaked database doesn't leak passwords.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **Security filter chain** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-17-storing-passwords/starter day-17-storing-passwords/solution
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

1. **`passwordEncoder()`**: `new DelegatingPasswordEncoder("pbkdf2", Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()))`. Stored as `{pbkdf2}…`.
2. **`PasswordStorageTest`**: No `{noop}`; prefix and no plaintext; salted (two hashes differ); `matches`.

## Points to land

- Hash, don't encrypt, passwords: salted and slow (OWASP 2025 names Argon2, yescrypt, scrypt, PBKDF2; bcrypt for legacy).
- The `{id}` prefix lets you migrate algorithms without a password reset.

## Common mistakes


## Questions for the room


## In our code (2 minutes)

Prepare before the session: find where our services store or check passwords and API keys (or which identity provider does it). Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Attacks nobody sees: tomorrow every failed login and refused lookup leaves an audit line.
