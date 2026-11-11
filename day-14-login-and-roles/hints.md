# Day 14 hints — Login and Roles for the Modern API

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Rules in the right order

1. Rules are checked top to bottom.
2. Put the specific rule (`GET /accounts/{id}`) before the general one (`/accounts/**`).
3. `.requestMatchers("/hello").permitAll()`, `.requestMatchers(HttpMethod.GET, "/accounts/{id}").hasAnyRole("CUSTOMER", "TELLER")`, `.requestMatchers("/accounts/**").hasRole("TELLER")`, `.anyRequest().authenticated()`.

## Controller tests break

1. A 403 on POST in a test, before your rules even apply, suggests different rules are running.
2. `@WebMvcTest` doesn't load your `SecurityConfig` unless you ask.
3. `@Import(SecurityConfig.class)` and `@WithMockUser(roles = "TELLER")` on the class; `@WithAnonymousUser` for the 401 test.
