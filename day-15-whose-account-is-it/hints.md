# Day 15 hints — Whose Account Is It? Fixing an IDOR

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Who is calling?

1. Spring can give a controller method the logged-in user.
2. Add an `Authentication` parameter; `getName()` is the username, `getAuthorities()` the roles.
3. Pass `caller.getName()` and an `isTeller` flag to a service method that checks `account.getUsername()`.

## 404 not 403

1. What would a 403 tell an attacker?
2. It confirms that account 2 exists.
3. Throw the same `AccountNotFoundException` you use for missing accounts.
