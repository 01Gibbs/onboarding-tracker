# Day 12 afternoon — Re-scan After the Upgrade

> **In plain English:** We scan again after the upgrade. Upgrading removes some problems and can add new ones, so 'upgraded' isn't the same as 'done'.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **the libraries under every box** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-12-rescan-after-migration/starter day-12-rescan-after-migration/solution
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

1. **Re-scan + Tomcat 10.1.61**: Far fewer findings, but Boot 3.5's open-source support ended 2026-06-30, so some are back.
2. **`FINDINGS.md`**: The Day 9 decisions closed with evidence; the remaining findings each have a fix version or a reason they're blocked.
3. **`build.gradle.kts`**: One new override, Tomcat 10.1.61, with a reason comment and a removal condition.

## Points to land

- Modernisation is a treadmill: the next hop is Boot 4 on Java 25, the Days 1–5 stack.
- Re-scan after every upgrade: the baseline you compare against is part of the work.

## Common mistakes

- Calling the migration done because the build is green.

## Questions for the room

- **Why did the scanner go *up* in CRITICALs after a successful upgrade?**
  <details><summary>Answer</summary>3.5.16 is the last open-source 3.5 release; new advisories keep arriving.</details>

## In our code (2 minutes)

Prepare before the session: find how our team tracks end-of-support dates for each service. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

A JDK-only upgrade, 17 → 21, done by inspecting first.
