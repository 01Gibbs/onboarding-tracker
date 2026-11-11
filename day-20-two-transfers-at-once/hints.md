# Day 20 hints — Two Transfers at Once

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Make the second commit fail

1. The database needs a way to notice that the row changed since you read it.
2. JPA has an annotation for a column that changes on every update and is checked when you write.
3. A `Long` field on `Account` with that annotation. Then look at the SQL Hibernate writes for an update: what does the extra condition do to a stale write?

## 409 instead of 500

1. What exception reaches the controller when the commit fails?
2. Spring translates Hibernate's exception into its own `OptimisticLockingFailureException` (a parent of `ObjectOptimisticLockingFailureException`).
3. Handle Spring's exception (not Hibernate's) in `ApiExceptionHandler`, the same way as your other handlers, with the status and title the brief asks for.
