# Day 11 hints — Upgrade to Spring Boot 3.5 and Java 17

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## `requestMatchers` doesn't compile

1. The code was rewritten for Spring Security 6. Which version is the compiler actually using?
2. `./gradlew -q dependencyInsight --dependency spring-security-config --configuration compileClasspath`.
3. Our own Day 9 overrides still pin the 5.x lines. Delete the whole override block; Boot 3.5 manages newer versions.

## A 404 became a 401

1. The test report says what changed. Which part of the request handling runs after the 404?
2. Spring Security 6 also authorizes the forward to the `/error` page.
3. `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` in the security rules.
