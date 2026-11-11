# Day 01 hints — Complete the Accounts CRUD API

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## 404 for a missing account

1. Where does "not found" get decided today? Who should decide it?
2. Look at `findById` in the service. `Optional` has a method that throws when it's empty.
3. `accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id))`, a `RuntimeException` you create in an `exception` package.

## One error format for every endpoint

1. You want one class that every controller's exceptions pass through.
2. Search the Spring docs for `@RestControllerAdvice` and `@ExceptionHandler`, and look at `ProblemDetail.forStatusAndDetail`.
3. Return a `ProblemDetail` from an `@ExceptionHandler(AccountNotFoundException.class)` method; set the title. For validation, handle `MethodArgumentNotValidException` and put `getFieldErrors()` into an `errors` property.
