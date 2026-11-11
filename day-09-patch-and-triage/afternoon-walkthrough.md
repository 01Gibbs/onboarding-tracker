# Day 09 afternoon — Patch What You Can, Decide the Rest

> **In plain English:** We fix the security problems we can by moving to safer library versions, and write down a reasoned decision for the ones we can't fix yet.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **the libraries under every box** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-09-patch-and-triage/starter day-09-patch-and-triage/solution
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

1. **`extra["…version"]` overrides**: The `io.spring.dependency-management` plugin reads version properties. Gradle `constraints {}` are silently ignored here.
2. **Red build 1**: The lockfile no longer matches: regenerate with `--write-locks`.
3. **Red build 2**: `contextLoads` fails: H2 2.x can't open a file written by 1.4. On a laptop, `rm -rf data`; in production, export with the old version and import with the new.
4. **`solution/FINDINGS.md`**: Before/after totals, a decision per finding, the conclusion paragraph.

## Points to land

- Patch bump inside a line = low risk; line jump = migration.
- Every override needs a reason and a removal condition in a comment.
- Some CRITICALs can't be fixed on Boot 2.7 at all (Spring Framework 5.3 / Security 5.7 are out of open-source support). That's the business case for Day 11.

## Common mistakes

- Deleting `data/` and moving on without noticing it's a production migration hazard.
- "Accepted risk" without a reason anyone could challenge.

## Questions for the room

- **H2's remaining HIGH is disputed by its maintainers. Is accepting it reasonable?**
  <details><summary>Answer</summary>More reasonable once the console is dev-only. That's tomorrow.</details>

## In our code (2 minutes)

Prepare before the session: find a version override in one of our builds. Does it say why, and when to remove it? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Configuration, not libraries: the actuator, the H2 console and a committed secret.
