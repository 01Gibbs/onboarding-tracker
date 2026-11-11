# Day 09 hints — Patch What You Can, Decide the Rest

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Red build after the overrides

1. Read the first line of the error: what does Gradle say it didn't resolve?
2. The lockfile still records the old versions.
3. `./gradlew dependencies --write-locks`, then build again.

## Second red build (contextLoads)

1. The error mentions the database file version.
2. The H2 version changed, and `data/` was written by the old one.
3. On a laptop: `rm -rf data`. In production you'd export with the old version and import with the new one.
