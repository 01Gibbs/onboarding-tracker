# How to use this course

Daily workshops for developers who are new to Java and joining a team that looks after Spring Boot services, old and new. Each day is **one question in the morning and its answer in the afternoon**. This page tells you how to run a day (facilitators) and how to work through one (learners). The list of days is in the [README](README.md).

---

## The shape of a day

| When | Facilitator | Learners | Material |
|---|---|---|---|
| **Morning, 30 min at most** | Deliver the day's brief: run its two-minute recall, walk through the task and its contract, show where to start | Listen, ask, then start | `brief.docx` |
| **During the day** | Answer in the chat. When someone's stuck, release **one** hint step at a time | Build it in `starter/`; check with tests and Bruno | `starter/`, `bruno/`, `hints.md` |
| **Afternoon, 20–30 min** | Walk through the solution, land the key points, show where it lives in your own services, set up tomorrow | Compare with your own work; ask | `afternoon-walkthrough.md`, `solution/` |

Each day folder (`day-NN-topic/`) holds exactly that:

```
day-NN-topic/
├── brief.docx               the question: overview, objectives, the exact contract, "Done when"
├── starter/                 where learners start: a complete Gradle (or, on Day 24, Maven) project
├── solution/                the answer (open it in the afternoon, not before)
├── bruno/                   Bruno requests that check the brief's contract (most days)
├── hints.md                 for the facilitator: three graded hints per task
└── afternoon-walkthrough.md for the facilitator: the afternoon session, timed
```

Day 0 is the team's own "Build an Onboarding Tracker REST API" workshop. Learners create their own project that day, so it has no `starter/`.

**Don't write code?** You're welcome too. Read [reference/start-here.md](reference/start-here.md) first: the twelve ideas the course is built on, in plain English, and how to follow a day. Every brief and every afternoon walkthrough opens with an **In plain English** summary for you.

---

## Before the first day

**Everyone**, the day before:

1. Install **JDK 25** ([Temurin](https://adoptium.net)), an IDE (IntelliJ IDEA recommended) and [Bruno](https://www.usebruno.com).
2. Get this repository onto your laptop.
3. From its top folder, run the setup check:

   ```bash
   ./tools/doctor.sh
   ```

   It checks your JDK, the Gradle wrapper, that port 8080 is free, that Gradle can build, that Bruno is installed and that every Bruno collection is readable. Every ❌ line tells you how to fix it. You don't need to install Gradle, a database or older JDKs: each project brings its own.

**Not writing code?** Install [Bruno](https://www.usebruno.com) only, if you'd like to watch the checks run; skip the JDK, the IDE and the setup check.

**Facilitators**, also:

- Read [docs/MISSION.md](docs/MISSION.md) (why the course exists) once.
- Keep [reference/troubleshooting.md](reference/troubleshooting.md) open: most chat questions are answered there.

---

## Running a day (facilitators)

**The day before (15 minutes)**

1. Read the day's `brief.docx`, then its `solution/`. To see exactly what changed:
   ```bash
   diff -r -x build -x target -x .gradle day-NN-topic/starter day-NN-topic/solution
   ```
2. Skim `hints.md`.
3. Prepare the **"In our code"** slot at the end of `afternoon-walkthrough.md`: it says what to find in your own team's services (a real security config, a real scan report, a real upgrade pull request). This is what turns the course into onboarding.

**Morning (30 minutes at most)**

1. Share `brief.docx`.
2. Two minutes of **"Before you start"** questions, answered from memory. The answers are at the end of the brief.
3. Walk through **Your task** and its contract tables. The exact paths, status codes and titles are what Bruno and the tests check.
4. Point at **Done when**: that's how learners know they've finished.

**During the day**

- Answer questions in the chat.
- When someone is stuck, post **step 1** of the matching hint in `hints.md` and wait before posting step 2. Step 3 nearly gives the answer; `solution/` is the whole answer.
- Some starters are **red on purpose**: tests or Bruno requests that fail until the day's work is done. That's the task, not a broken course.

**Afternoon (20–30 minutes)**

Follow `afternoon-walkthrough.md`: recap, the solution tour in order, points to land, common mistakes, questions for the room, "In our code", and tomorrow's question.

---

## Working through a day (learners)

1. **Go to the day's starter.** Every command runs from there:
   ```bash
   cd day-NN-topic/starter
   ls gradlew        # "No such file or directory"? You're in the wrong folder.
   ```
2. **Run it:** `./gradlew bootRun` starts the app on port 8080. `./gradlew test` runs the tests; the report is at `build/reports/tests/test/index.html`. On a Maven day (there's an `mvnw` instead of `gradlew`), it's `./mvnw spring-boot:run` and `./mvnw test`, with results in `target/surefire-reports/`.
3. **Check your work in Bruno:** **Open Collection** → the day's `bruno/` folder → pick the **Local** environment → **Run**. Every request shows ✅ or ❌. A red request on the starter is the to-do list. More in [reference/bruno.md](reference/bruno.md).
4. **Start again from a clean database** at any time: stop the app, `rm -rf data`, start it again.
5. **You're done** when the brief's **Done when** line is true.
6. **Stuck?** Ask in the chat. You'll get a hint, not the answer.

### Reference while you work

| When you… | Open |
|---|---|
| …want the big picture without the code | [reference/start-here.md](reference/start-here.md) |
| …meet a word you don't know | [reference/glossary.md](reference/glossary.md) |
| …see an error or a strange status code | [reference/troubleshooting.md](reference/troubleshooting.md) |
| …want the Java idioms behind the code (`BigDecimal`, `Optional`, records…) | [reference/java-idioms.md](reference/java-idioms.md) |
| …want to see where today's change fits in the app | [reference/accounts-api.md](reference/accounts-api.md) |
| …are on <!-- course:legacy-days -->Days 6–13, 28–29<!-- /course:legacy-days --> (the older service) | [reference/legacy-security-cheatsheet.md](reference/legacy-security-cheatsheet.md) |
| …are on the security days | [reference/owasp-top-10-cheatsheet.md](reference/owasp-top-10-cheatsheet.md) |

---

## Changing the course

Everything about each day (folder, title, test totals, what's red on purpose) is stated once in `tools/course/course.json`. The briefs, walkthroughs and tables are generated from it, and one command proves the whole course still works. See [docs/MAINTAINING.md](docs/MAINTAINING.md).
