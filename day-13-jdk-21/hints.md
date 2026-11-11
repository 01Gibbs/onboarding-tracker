# Day 13 hints — Upgrade the JDK from 17 to 21

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Which JDK does what

1. There are three: your shell's, Gradle's and the compiler's.
2. `java -version`, `./gradlew -version`, `./gradlew -q javaToolchains`.
3. Shell: 25; Gradle daemon: 17 (from `gradle-daemon-jvm.properties`); toolchain: whatever `JavaLanguageVersion.of(...)` says.

## Is 21 supported?

1. Primary sources only.
2. Spring Boot 3.5's system requirements page, and Gradle's compatibility matrix.
3. Boot 3.5: Java 17 to 25. Gradle 8.14: Java 21 toolchains are supported, so the daemon can stay on 17.
