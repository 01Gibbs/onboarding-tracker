# Day 06 hints — Get a 2022 Service Running, and Audit It

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## The `What went wrong: 25.0.x` error

1. Two different JDKs are involved in a Gradle build. Which one is too new?
2. Look at `gradle/gradle-daemon-jvm.properties` and `./gradlew -version`.
3. Gradle 8.14 itself can't run on Java 25; that file tells Gradle to run its daemon on JDK 17. The toolchain (11) is separate.

## Finding legacy markers

1. Compare imports and annotations with Days 1–5.
2. `grep -r "javax\." src`, `grep -r WebSecurityConfigurerAdapter src`, `grep -r "org.junit.Test" src`.
3. `javax.*` (now `jakarta.*`), the security adapter, getter/setter DTOs (now records), JUnit 4, `@MockBean`, the `h2.version` pin.
