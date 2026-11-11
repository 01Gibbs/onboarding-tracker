# Day 01 afternoon — Complete the Accounts CRUD API

> **In plain English:** We finish a service that keeps bank-style accounts: changing and deleting them, and giving clear, consistent error messages when something is asked for that doesn't exist or doesn't make sense.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`ApiExceptionHandler`** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-01-complete-the-crud-api/starter day-01-complete-the-crud-api/solution
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

1. **`exception/AccountNotFoundException.java`**: A plain `RuntimeException` (unchecked). The service throws it; it knows nothing about HTTP.
2. **`service/AccountService.java`**: `getById` uses `findById(id).orElseThrow(() -> new AccountNotFoundException(id))`; `updateOwner` reuses `getById`; `delete` checks `existsById` first.
3. **`controller/AccountController.java`**: `@PutMapping` with `@Valid @RequestBody UpdateAccountRequest`; `@DeleteMapping` + `@ResponseStatus(NO_CONTENT)`. The controller has no `if (missing)` anywhere.
4. **`exception/ApiExceptionHandler.java`**: `@RestControllerAdvice` with two `@ExceptionHandler`s returning `ProblemDetail`. The validation handler builds the `errors` map from `getFieldErrors()`.

## Points to land

- Throw in the service, translate in one place. Every endpoint that throws `AccountNotFoundException` gets the same 404 for free.
- `ProblemDetail`'s `status` sets the HTTP status, and the content type becomes `application/problem+json` (RFC 9457).
- Spring Boot hides exception messages by default. Only put in `detail` what's safe for the caller to see.
- Why `UpdateAccountRequest` has no currency: changing the currency of an account holding money isn't an update.

## Common mistakes

- Returning `ResponseEntity.notFound()` from every controller method instead of throwing: duplicated, and easy to forget on a new endpoint.
- Keeping `@ResponseStatus` on the exception *and* a handler: two places to change.
- `DELETE` returning 200 with a body, or 204 for a missing id.

## Questions for the room

- **Should a second `DELETE` of the same account be 404 or 204?**
  <details><summary>Answer</summary>Both are defended in practice. 404 is honest; 204 makes `DELETE` idempotent for retries. Pick one and be consistent.</details>
- **What does `GET /accounts/abc` return, and why isn't it a problem detail?**
  <details><summary>Answer</summary>400 from Spring's own type conversion, which our handler doesn't cover. `spring.mvc.problemdetails.enabled=true` covers Spring's errors too.</details>

## In our code (2 minutes)

Prepare before the session: find our services' `@ControllerAdvice` (or whatever produces error bodies). Is it RFC 9457? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

A colleague's pull request built on today's API, with six bugs in it. Tomorrow is a code review.
