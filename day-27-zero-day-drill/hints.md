# Day 27 hints — Zero-Day Drill

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Which services ship log4j-core?

1. Look at what each service ships, not at what it mentions. None of the services' build files say "log4j-core".
2. `dependencyInsight` per project, on the configuration that's packaged: `runtimeClasspath`. A project's tasks are addressed by its path, e.g. `:services:payments:`.
3. `./gradlew -q :services:statements:dependencyInsight --dependency log4j-core --configuration runtimeClasspath`. Then try notifications with `--dependency org.apache.logging.log4j`: what does it ship instead, and is that jar affected?

## Upgrade it

1. Ship a current release, and be ready to show it fixes *every* Log4j CVE from December 2021, not just the first one.
2. Look on Maven Central for the newest 2.x release (not a beta). Apache's security page and NVD list the fixed versions per CVE: is your release past all of them?
3. The version catalog has a `log4j` entry. Did changing it turn every test green? If not, search every `build.gradle.kts` for `2.14.1`: the estate needs one version, set in one place.

## Prove and report

1. The tests read the version from the jar that's actually loaded. Read their failure message.
2. Re-run `dependencyInsight` after the fix and paste the before and after into `INCIDENT.md`.
3. In section 5, say why notifications isn't affected, with the test that proves it, and remind the reader that nothing is fixed until the new build is deployed.
