# Day 18 hints — Log Security Events

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Hearing about failed logins

1. Spring Security publishes an event when a login fails.
2. `@EventListener` on a method taking `AbstractAuthenticationFailureEvent`, in a `@Component`.
3. Log `event.getAuthentication().getName()` and `event.getException().getClass().getSimpleName()`. Never the credentials.

## No fake log lines

1. What could a username containing a newline do to a log file?
2. Clean caller-supplied values before logging them.
3. Replace `\r` and `\n` with `_` in a small helper used by every audit line.
