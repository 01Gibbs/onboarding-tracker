# Day 13 afternoon — Upgrade the JDK from 17 to 21

> **In plain English:** We move the older service to a newer Java version. We check what it uses first, then change it, then prove it still works.

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **the JDK under the whole app** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-13-jdk-21/starter day-13-jdk-21/solution
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

1. **Inspection output**: `java -version` (shell), `./gradlew -version` (daemon JVM 17), the toolchain, `./gradlew javaToolchains`, `dependencyInsight` showing the requested JVM version, `jdeps --jdk-internals` (only `sun.misc.Unsafe` via `jdk.unsupported`).
2. **Evidence**: Boot 3.5 system requirements: Java 17 up to 25. Gradle: Java 21 toolchains from 8.4. The daemon can stay on 17.
3. **The change**: `JavaLanguageVersion.of(21)`: one line. Log says `using Java 21`; lockfile unchanged.
4. **The ticket**: Docker base image, CI JDK, `release`, `-Xlint` deprecations, OpenRewrite `UpgradeToJava21`.

## Points to land

- Inspect → check support from primary sources → change → prove. The same pattern for any upgrade.
- The daemon JDK and the toolchain JDK are independent.
- The JEP 451 dynamic-agent warning in test output is a real 21 effect (Mockito).

## Common mistakes

- Changing `JAVA_HOME` and assuming the build uses it.
- Forgetting the runtime: the Docker image still on 17.

## Questions for the room

- **What would you check before 21 → 25?**
  <details><summary>Answer</summary>Gradle's compatibility matrix (8.14 doesn't list 25), Boot support, and libraries using JDK internals.</details>

## In our code (2 minutes)

Prepare before the session: find the Dockerfile base image and CI JDK for one of our services. Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

We leave the legacy service here: it's on a supported Boot line, Java 21, patched and hardened. Tomorrow we go back to the **modern** Accounts API from Day 5 (Boot 4, Java 25), which has no security at all, and spend Days 14–19 on the OWASP Top 10 risks that matter most for a service that handles money. Ask the room to re-read Day 6's security-config table tonight: tomorrow they write one themselves.
