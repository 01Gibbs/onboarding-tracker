# Day 28 hints — Patch the Container, Not Just the Code

For the facilitator. When someone's stuck, release **one** step at a time in the chat, and wait before releasing the next.
Step 1 is a nudge, step 2 says where to look, step 3 nearly gives the answer. `solution/` is the full answer.

## The right base image

1. Two things are wrong with `FROM openjdk:11-jre-slim`. Read the test failures: each names one.
2. The `openjdk` page on Docker Hub (read the top of it), then the "Supported tags" list on the `eclipse-temurin` page. Which number does the toolchain in `build.gradle.kts` use?
3. An `eclipse-temurin` JRE tag that starts with 21 and names an Ubuntu release: `21-jre-` followed by a codename from the tag list. Any other image (even one tagged `21…`) isn't a Java runtime image the test accepts.

## Not root

1. Docker runs the container as the last `USER` in the final stage. With none, the process is root.
2. Docker's best-practices page, section USER. You need a user that exists in the image, then a `USER` line (by convention just before `ENTRYPOINT`).
3. `RUN groupadd … && useradd …` creating an `app` user with a fixed uid, then `USER <uid>:<gid>`. Numbers work better than names for Kubernetes `runAsNonRoot`.

## It starts, then fails to write

1. What does the app write to disk, and where?
2. `spring.datasource.url` in `application.properties` is a relative path. Relative to which folder in the container? Who owns it?
3. `WORKDIR /app`, and create `/app/data` owned by your app user in the same `RUN` as `useradd`. Leave the jar itself owned by root.
