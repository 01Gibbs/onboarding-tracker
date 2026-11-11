# Day 08 hints — What Do We Ship? Lockfile, Scan, Trace

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Writing the lockfile

1. Gradle has a feature called dependency locking.
2. Add `dependencyLocking { lockAllConfigurations() }` to `build.gradle.kts`.
3. Then `./gradlew dependencies --write-locks` writes `gradle.lockfile`.

## Tracing a library

1. You want to know *why* a version is on the classpath.
2. `./gradlew -q dependencyInsight --dependency <name> --configuration runtimeClasspath`.
3. Read the output bottom-up: the last line is what pulled it in. "Selected by rule" means a version property, possibly ours.
