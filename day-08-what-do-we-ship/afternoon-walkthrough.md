# Day 08 afternoon — What Do We Ship? Lockfile, Scan, Trace

> **In plain English:** Every service is built from other people's code (libraries). We list exactly which ones we ship and check them against public lists of known security problems.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the HTML test report (`build/reports/tests/test/index.html`), and a terminal in `solution/`.

Where today's change sits in the app: **the libraries under every box** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-08-what-do-we-ship/starter day-08-what-do-we-ship/solution
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

1. **`./gradlew -q dependencies --configuration runtimeClasspath`**: `->` = a different version was chosen; most of the tree is transitive.
2. **`dependencyLocking { lockAllConfigurations() }` + `gradle.lockfile`**: A flat list scanners read; the build now fails if versions drift.
3. **The scan**: Totals, then triage. Don't panic at the number.
4. **`dependencyInsight --dependency h2`**: "Selected by rule": **our** pin. SnakeYAML comes from `spring-boot-starter`.
5. **`solution/FINDINGS.md`**: Compare top-findings tables and first impressions.

## Points to land

- You ship what's on `runtimeClasspath`, not what you typed.
- Flagged ≠ exploitable. SnakeYAML's CVE-2022-1471 in a Boot app is the classic example (spring-boot#33457).
- OWASP A03:2025 Software Supply Chain Failures.

## Common mistakes

- Scanning `build.gradle.kts` instead of the resolved versions.
- Treating the advisory count as the risk.

## Questions for the room

- **Which finding did *we* cause?**
  <details><summary>Answer</summary>The `h2.version = 1.4.200` pin "for now" from 2021.</details>

## In our code (2 minutes)

Prepare before the session: find our CI's dependency-scan report for one service. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Fix what a patch bump can fix, and write down a decision for the rest.
