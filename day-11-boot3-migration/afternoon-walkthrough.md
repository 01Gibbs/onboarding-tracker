# Day 11 afternoon — Upgrade to Spring Boot 3.5 and Java 17

> **In plain English:** We move the older service onto a supported version of its framework and Java, using a tool that does most of the edits, then fix what the tool misses.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **the whole app's platform** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-11-boot3-migration/starter day-11-boot3-migration/solution
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

1. **`rewrite.patch` (dry run)**: `javax` → `jakarta`, `SecurityFilterChain` bean, `requestMatchers`, lambdas, toolchain 17, Boot 3.5.16. And an odd `jaxb-api` line.
2. **❌ `requestMatchers` doesn't compile**: Our own `extra["spring-security.version"] = "5.7.13"` from Day 9 still pins Security 5. OpenRewrite doesn't know about our overrides: delete the whole block.
3. **Lockfile**: Delete and regenerate after removing the plugin; `rm -rf data` (H2 file format again).
4. **`@MockBean` → `@MockitoBean`**: Deprecated in 3.5, removed in Boot 4: clear it now.
5. **❌ 404 became 401**: Security 6 authorizes error dispatches: `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`. Day 10's regression test caught it.

## Points to land

- Read the patch before applying it. Tools don't know your intent or your overrides.
- Regression tests are what make a big upgrade safe.

## Common mistakes

- Regenerating the lockfile before removing the overrides (it locks the wrong versions).
- Deleting the failing H2 test instead of understanding the 401.

## Questions for the room


## In our code (2 minutes)

Prepare before the session: find our most recent framework-upgrade pull request. What did it fix by hand? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

The build is green on Boot 3.5. Tomorrow: did the upgrade actually fix the findings? Re-scan and decide.
