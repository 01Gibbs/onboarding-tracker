# Day 23 afternoon — Mask What You Show

> **In plain English:** We hide most of each account number in what the service shows and records, so a screenshot or a log file can't leak a customer's full details.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **`AccountResponse`, the transfer log line** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-23-mask-what-you-show/starter day-23-mask-what-you-show/solution
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

1. **The colleague's change**: `accountNumber` on `Account` (random 8 digits via `SecureRandom`, seeds 12345678 / 87654321), in full in `AccountResponse`, and in full in a new INFO log line.
2. **`config/Mask.java`**: One helper next to Day 18's `AuditLog`: null stays null, short values → `****`, otherwise `****` + the last 4.
3. **`dto/AccountResponse.from`**: The single exit point for every account JSON: get, list, `/mine` and create are all covered by one call.
4. **`AccountService.transfer` log line**: `TRANSFER from=****5678 to=****1513 amount=50.00 GBP`.
5. **The tests**: The log test also asserts the masked line *is* there, so deleting the log line doesn't pass.

## Points to land

- Data leaves a service through responses, logs and errors. Mask all three.
- Mask at the edge (display), never in storage: the stored number must stay intact.
- One masking function, used everywhere, so the rules can't drift.

## Common mistakes

- Masking in the entity getter (breaks storage and lookups).
- `@JsonIgnore` (customers need the last 4).
- Masking one endpoint and missing the list or create response.
- `substring` on null or short values.

## Questions for the room

- **Who should ever see a full account number through this API?**
  <details><summary>Answer</summary>Possibly the customer themselves, behind a deliberate, audited action. Never by default, and never in logs.</details>

## In our code (2 minutes)

Prepare before the session: find our logs for one service: anything that looks like a full account number, card number or email? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: Day 5's API again, but built with Maven, as many of the team's services are. One dependency ticket to take through a build you haven't used yet.
