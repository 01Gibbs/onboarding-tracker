# Day 16 hints — Find and Fix an Injection

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Why the test fails

1. Write out the query string with the test's input pasted in.
2. `... AND a.currency = 'GBP' OR '1'='1'`. What does `OR '1'='1'` do to the WHERE?
3. It's always true, so every account matches, including Grace's.

## The fix

1. Don't build the query string yourself.
2. Spring Data can write this query from a method name, with bound parameters.
3. A query method on `AccountRepository` whose name says both conditions (username *and* currency); then the `EntityManager` code has nothing left to do.
