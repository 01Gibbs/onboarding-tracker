# Day 23 hints — Mask What You Show

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Mask in one place

1. Two places show account numbers: responses and a log line. How do you stop them drifting apart?
2. A small static helper next to Day 18's `AuditLog`.
3. `Mask.accountNumber(n)`: null stays null; fewer than 4 characters → `"****"`; otherwise `"****" + n.substring(n.length() - 4)`. Call it from `AccountResponse.from` and the log line.

## Which responses?

1. How many endpoints return an account?
2. Every one of them builds its JSON the same way.
3. `AccountResponse.from` is the single exit point: mask there, not in the entity (the stored number must stay intact).
