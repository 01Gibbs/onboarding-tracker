# Day 26 hints — Triage a Scanner Report

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## Triage the six findings

1. Read the code at each location before deciding anything. The tool sees patterns; you see what the code does.
2. Two findings point at the same line. Two others are in places that never run in production or never build a query from text. Compare `locations`, and look at `src/test` vs `src/main`.
3. SAST-103: find the Day 16 test that tries to inject through `currency`; that's your evidence. SAST-104: is test code packaged into the jar? SAST-105 is real but a dev tool: what limits the damage today, and what would make you fix it?

## `everyFixRemovedTheFlaggedCode` is red

1. A Fix is a claim about the code, and the test checks it. Read its message: which finding, which file?
2. Each SARIF result's `region.snippet` is the code the scanner flagged. Is that code still in the file?
3. Either change the code so the flagged line is really gone, or the decision isn't Fix: make it a False positive or an Accept risk, with a record in `reports/decisions/`.

## Get the key out of the code

1. The value must come from outside the code, and the test sets it through a property.
2. Spring can inject a property into a constructor parameter. Which property name does `NoHardcodedSecretsTest` set?
3. A constructor parameter `@Value("${notifications.api-key}") String apiKey`, and in `application.properties` a line that reads it from `NOTIFICATIONS_API_KEY` with an empty default, so the app still starts locally. Then think about rotation.

## Replace MD5

1. `TokenHashingTest` tells you which algorithm it expects.
2. The algorithm is a string passed to `MessageDigest.getInstance`. The Java docs list the standard names.
3. `"SHA-256"`. Why is one fast hash fine here but not for passwords? (Tokens are long and random.) Existing MD5 hashes can't be converted: the tokens must be reissued.
