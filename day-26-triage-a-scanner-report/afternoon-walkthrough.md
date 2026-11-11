# Day 26 afternoon — Triage a Scanner Report

> **In plain English:** The organisation's security scanner has checked our code and sent a list of possible problems. Some are real and we fix them; some are mistakes by the tool; one is real but we choose to live with it for now. Today we decide for each one, and write down why.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **`NotificationsClient`, `TokenHasher` and the scan paperwork** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-26-triage-a-scanner-report/starter day-26-triage-a-scanner-report/solution
```

## Agenda (25 min)

| Time | What |
|---|---|
| 0–3 | Recap the question. Show of hands: who got it green? Where did people get stuck in the chat today? |
| 3–14 | Solution tour (below), in order |
| 14–19 | Points to land, and common mistakes |
| 19–22 | Questions for the room |
| 22–25 | In our code, then tomorrow's question |

## Solution tour

1. **`reports/sast-report.sarif`**: `runs[0].tool.driver.rules` (id, CWE, help link, default level) vs `runs[0].results` (ruleId, message, `locations[].physicalLocation`, `properties.findingId`). Real tools track a finding across scans with `guid` / `partialFingerprints`; our short ids live in a property bag, which SARIF allows.
2. **`NotificationsClient`, before and after**: `static final String API_KEY = "ntf_live_…"` → a constructor `@Value("${notifications.api-key}")`, and `notifications.api-key=${NOTIFICATIONS_API_KEY:}` in `application.properties`. `NoHardcodedSecretsTest`: one test scans `src/main` like a secrets scanner, the other proves the value comes from configuration.
3. **`TokenHasher`**: `"MD5"` → `"SHA-256"`, and `TokenHashingTest`'s FIPS 180-2 test vector for `"abc"`.
4. **`TRIAGE.md`**: The six rows: SAST-101 and 102 Fix (101's justification says the key was rotated), 103 and 104 False positive, 105 Accept risk, 106 Duplicate of 101.
5. **`reports/decisions/SAST-103.md` and `SAST-105.md`**: A false positive with test evidence (Day 16's `AccountSearchTest`), and an accepted risk: what could go wrong, why it's low today, the plan, the owner, the review date (2027-02-10), and what ends the acceptance.
6. **`TriageTest`**: The paperwork checked against the code, so a release can't go out with an undecided finding. A Fix must really remove the flagged code (the SARIF `snippet`, whitespace ignored), and a Duplicate must name another finding on the same line: so calling the Spring Data query or the test password "Fix" can't dodge the decision record.

## Points to land

- A scanner report is input, not a verdict. Every finding needs a decision **and evidence**: code, a test, or a link.
- "Accept risk" is a decision someone owns, with an expiry date, not "ignore". Engineers recommend; a risk owner accepts.
- Fixing a leaked secret in code is half the job: rotate it, because it lives on in Git history and in every clone.
- Fast hashes (SHA-256) are right for long random tokens; slow salted ones (Day 17) for passwords. MD5 and SHA-1 are wrong for both.

## Common mistakes

- Marking SAST-103 a false positive "because we use JPA", without evidence. Point to the Day 16 test.
- Moving the key into `application.properties` as a literal: still a secret in the repository, and `noSecretLiteralsInMainCode` stays red.
- Treating the duplicate as a second piece of work, or closing SAST-101 as a duplicate of SAST-106 without fixing anything.
- An accepted risk with no owner or review date.
- Marking a false positive or the accepted risk "Fix" to skip the record: `everyFixRemovedTheFlaggedCode` fails, because the flagged code is still there.

## Questions for the room

- **The scanner flags the same false positive every week. What do you do?**
  <details><summary>Answer</summary>Suppress it in the tool with a link to the decision record (most tools have a "false positive" status or a suppression comment), so the decision travels with the code. Don't just ignore the report.</details>
- **Who can accept a risk?**
  <details><summary>Answer</summary>Not the developer alone: the service's risk owner (often a product or engineering lead), under the organisation's risk process. The developer writes the analysis.</details>

## In our code (2 minutes)

Prepare before the session: find the latest SAST report for one of our services (in the scanner's dashboard or the pipeline's artifacts). Pick three findings: what did the team decide, who decided, and when is each due for review? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: a critical CVE is announced in a library half the world uses. You have hours, not weeks: which of our services ship it? A zero-day drill.
