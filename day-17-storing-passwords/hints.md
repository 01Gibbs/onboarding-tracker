# Day 17 hints — Store Passwords Properly

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## A password encoder

1. Spring Security uses a `PasswordEncoder` bean for every login.
2. `DelegatingPasswordEncoder` reads the `{id}` prefix to choose the algorithm.
3. `new DelegatingPasswordEncoder("pbkdf2", Map.of("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8()))` as a `@Bean`.

## Getting the hashes

1. You need to encode each demo password once.
2. A throwaway test can print `passwordEncoder().encode("ada-pass")`.
3. Run it with `./gradlew test --tests '*YourTest*' -i`, copy the `{pbkdf2}…` values into `SecurityConfig`, delete the test.
