# Day 19 hints — All or Nothing: Safe Transfers

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Make it all-or-nothing

1. Two changes must succeed or fail together.
2. Spring wraps a method in a database transaction when you ask.
3. `@Transactional` on `transfer`: if a `RuntimeException` escapes, every change is rolled back.

## Check before you change

1. In which order does the colleague's code load and change things?
2. Load and check both accounts before touching either balance.
3. Move `getById(toId)` above `from.withdraw(amount)`.
