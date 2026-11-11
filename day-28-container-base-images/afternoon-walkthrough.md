# Day 28 afternoon — Patch the Container, Not Just the Code

> **In plain English:** Our service doesn't run on its own: it ships inside a container image, which also holds a Java runtime and a small operating system. Those parts get security fixes too, and many findings in a scan are there, not in our code. Today we make the image use a supported, up-to-date base, the same Java version as the app, and an ordinary user instead of the all-powerful "root".

**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). Have open: `solution/` in the IDE, the Bruno collection in `bruno/`, and a terminal in `solution/`.

Where today's change sits in the app: **the container image around the whole app** (see the [Accounts API diagram](../reference/accounts-api.md)).

To show everything that changed today, from the course root:

```bash
diff -r -x build -x target -x .gradle day-28-container-base-images/starter day-28-container-base-images/solution
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

1. **`starter/Dockerfile`**: Three problems: a deprecated image, Java 11 against a Java 21 toolchain, and no `USER`. Show the `openjdk` page's deprecation notice on Docker Hub, and that the tag no longer exists.
2. **`DockerfileTest`**: It runs without Docker: it reads two text files (joins `\` lines, last `FROM` = final stage, which must be `eclipse-temurin` at the toolchain's Java version, so `ubuntu:21.04` doesn't pass; the stage's last `USER` is the one Docker uses, even after `ENTRYPOINT`). A cheap guard that would have caught Days 11 and 13 forgetting the image.
3. **`solution/Dockerfile`**: `eclipse-temurin:21-jre-resolute` (a JRE, so less to patch; a named OS release, so moving to the next one is a decision), the `groupadd`/`useradd` line, `/app/data` owned by the app user, numeric `USER 10001:10001`, and the comment on how patches arrive (`docker build --pull`).
4. **`solution/.dockerignore`**: Keeps `data/`, `.gradle/` and `src/` out of the build context: no local H2 data in the image.
5. **With Docker, if the presenter has it**: `docker run --entrypoint id`, then `trivy image` and compare the OS findings. (These steps weren't run when the day was built: check them beforehand.)

## Points to land

- The image is part of what we ship. A clean lockfile scan says nothing about the Java runtime or OS in the image.
- Images don't patch themselves: a floating tag only helps if you rebuild and redeploy. Pinning by digest trades automatic fixes for repeatability, so it needs a bot (Renovate, Dependabot) or a routine.
- The runtime Java must equal or exceed the compile target. A JDK upgrade ticket (Day 13) must include the image.
- Non-root is a cheap layer of defence in depth; many platforms (Kubernetes `runAsNonRoot`, OpenShift) refuse or randomise root anyway.

## Common mistakes

- Swapping `openjdk:11-jre-slim` for `eclipse-temurin:11-jre`: supported, but the app is Java 21 (the first test catches it).
- `USER app` without creating the user: `docker run` fails with "unable to find user app".
- A non-root user with the default working directory `/`: the app starts, then H2 can't create `./data`.
- Overriding `ENTRYPOINT` and losing the image's `/__cacert_entrypoint.sh`. It only matters with `USE_SYSTEM_CA_CERTS`; worth knowing, not a bug here.

## Questions for the room

- **Why `21-jre-resolute` and not `latest` or `21`?**
  <details><summary>Answer</summary>`latest` can jump Java versions under you; `21` is a JDK (bigger, more to patch); naming the OS release makes an Ubuntu upgrade a decision, not a surprise.</details>
- **The scan shows a HIGH finding in an OS package with no fix available. What now?**
  <details><summary>Answer</summary>As on Day 9: record a decision (not reachable, or accepted until a fix ships), and rebuild when the base image publishes the fix. A smaller base (`-alpine`, `ubi-minimal`, `jlink`) leaves less to find.</details>

## In our code (2 minutes)

Prepare before the session: find the `Dockerfile` (or Jib or buildpacks configuration) of one of our services. Which base image and tag does it use? Does its Java version match the build's? Does it set `USER`? When was the image last rebuilt? Show it on screen and ask the room what's the same as today's solution, and what's different.

## Tomorrow

Tomorrow: the same service moves to Spring Boot 4 on Java 25, the team's modern platform, and the Java version in the image has to move with it.
