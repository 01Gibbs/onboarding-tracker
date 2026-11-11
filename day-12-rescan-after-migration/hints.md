# Day 12 hints — Re-scan After the Upgrade

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Why CRITICALs came back

1. Look at the release date of Boot 3.5.16 against today's date.
2. Check spring.io's support dates for 3.5.
3. 3.5.16 is the last open-source 3.5 release; advisories published since then have no fix on that line.

## Patch what a patch can still fix

1. Same trick as Day 9.
2. Look up the fixed version on osv.dev, then check Maven Central for a newer release in the same line.
3. `extra["tomcat.version"] = "10.1.x"` with a reason comment; regenerate the lockfile; re-scan.
