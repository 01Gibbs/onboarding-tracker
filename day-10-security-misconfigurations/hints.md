# Day 10 hints — Fix the Security Misconfigurations

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Hide actuator endpoints

1. Two separate things: which endpoints exist over HTTP, and who may call them.
2. `management.endpoints.web.exposure.include`, and the matchers in `SecurityConfig`.
3. `include=health,info`, and `.antMatchers("/actuator/health", "/actuator/info").permitAll()` instead of `/actuator/**`.

## H2 console only in dev

1. Spring can load extra properties for a named profile.
2. `application-dev.properties` is read only when the `dev` profile is active.
3. Move `spring.h2.console.enabled=true` there; delete both console lines from `application.properties`.
