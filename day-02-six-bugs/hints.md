# Day 02 hints — Code Review: Six Bugs

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Bug 1 (GBP rejected)

1. The log says GBP is unsupported. Read the check that decides it.
2. How does Java compare two `String`s' contents? What does `==` compare?
3. `c == "GBP"` compares object identity. Use `"GBP".equals(c)`, or better, a `Set.of("GBP", "EUR", "USD").contains(c)`.

## Bug 2 (0.1 becomes 0.1000000000000000055…)

1. Which type holds the opening balance in the request?
2. `new BigDecimal(double)` copies the double's binary error exactly.
3. Make `openingBalance` a `BigDecimal` in the DTO (with `@NotNull`) and pass it straight through.

## Bugs 3–6

1. Compare each method with Day 1's solution: annotations, return types, mappings.
2. Bug 3 is a missing annotation; bug 4 a return type; bug 5 an `Optional` method; bug 6 a mapping.
3. `@Valid`; `ResponseEntity.created(uri).body(...)`; `orElseThrow(...)` not `.get()`; `@DeleteMapping("/{id}")` + `@ResponseStatus(NO_CONTENT)`.
