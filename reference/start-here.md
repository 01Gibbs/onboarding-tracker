# Start here if you don't write code

This course trains developers, but analysts, testers, project managers, risk and security colleagues follow it too. You don't need to write any code to get a lot from it. This page gives you the dozen ideas everything else is built on, and shows you how to follow a day.

Related: [Glossary](glossary.md) (every term, A to Z) · [How to use the course](../HOW-TO-USE.md)

---

## The picture in one paragraph

The team looks after **services**: programs that run on servers and answer requests from other programs, such as a mobile app asking "what's this customer's balance?". Most are written in **Java** using a toolkit called **Spring Boot**. Some services are new and some are years old. The team's two big jobs are **security remediation** (fixing weaknesses before someone abuses them) and **platform modernisation** (moving old services onto supported versions of Java and their libraries, so they keep receiving security fixes).

---

## Twelve ideas, in plain English

| Idea | What it means | You'll see it when… |
|---|---|---|
| **Service** | A program that runs all the time on a server, waiting for requests. | Every day: we build or change one. |
| **API** | The list of requests a service accepts and what it sends back, like a menu. [MDN](https://developer.mozilla.org/en-US/docs/Glossary/API) | A brief's "Your task" tables list it exactly. |
| **Request and response** | A program asks (request: "give me account 7"); the service answers (response: the account, or an error). | Bruno shows both side by side. |
| **Status code** | A three-digit number on every response: **200** OK, **201** created, **400** your request was wrong, **401** who are you?, **403** you're not allowed, **404** not found, **500** the service broke. [MDN](https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status) | Most checks in the course are "did we get the right code?" |
| **Java, and its version** | The programming language. Each version is supported for a limited time; after that it stops getting security fixes. | Days 6, 11, 13, 25, 29: moving to newer versions. |
| **Spring Boot** | A toolkit that does the plumbing (web, database, security) so developers write only the business logic. It has versions too. | Days 11 and 29: upgrading it. |
| **Library (dependency)** | Code written by someone else that a service reuses. A typical service ships with 50–150 of them. | Days 8–9, 26–27: checking them for known problems. |
| **Vulnerability, CVE** | A known security weakness. Each public one gets an id like `CVE-2021-44228`. [CVE.org](https://www.cve.org/About/Overview) | Scan reports list them; our job is to fix or decide. |
| **Build tool (Gradle, Maven)** | The program that downloads the libraries, compiles the code and runs the tests. | `./gradlew test` or `./mvnw test` in every day. |
| **Test** | A small program that checks the service does what it should and says pass (green) or fail (red). | Every day. "Red on purpose" means a test the learner must make pass. |
| **Bruno** | An app that sends requests to a service and checks the responses, without writing code. | Most days have a `bruno/` folder of ready-made checks. |
| **Terminal** | A text window where you type commands instead of clicking. | The commands in every brief run there. |

---

## How to follow a day without writing code

1. **Morning (30 minutes at most):** listen to the brief. Read its **In plain English** box first, then **Overview** and **Done when**. Skip the code tables if you like: they're the exact rules developers must meet.
2. **During the day:** watch the chat. The questions people ask show you where the real difficulty is, and that's useful to anyone planning or assessing this work.
3. **Afternoon (20–30 minutes):** this is the session for you. The walkthrough opens with the same plain-English summary, then shows the answer, the mistakes people made, and **where the same thing lives in the team's own services**.
4. **Ask three questions** in any walkthrough. They're always welcome:
   - *Why does this matter to the business or to customers?*
   - *What would have happened if we hadn't fixed it?*
   - *How would we know it's fixed? What's the evidence?*

### If you'd like to try something hands-on

Sit with a developer for ten minutes on a day with a `bruno/` folder and ask them to start the app. Then, in Bruno: **Open Collection** → the day's `bruno/` folder → choose the **Local** environment → **Run**. Every check turns ✅ or ❌. Run it against the starter (some ❌ on purpose) and then against the solution (all ✅): that's the whole day's work, in one screen. [More on Bruno](bruno.md).

---

## The days that matter most if you work on risk, security or planning

| Days | Why they matter outside development |
|---|---|
| 8–9 | How the team finds out what it ships, which parts are vulnerable, and how a "we're not fixing this yet" decision gets written down |
| 10, 14–18 | The common security mistakes ([OWASP Top 10](https://owasp.org/Top10/2025/)) and what fixing each looks like |
| 11–13, 29 | What an upgrade really involves, and why "the tool did it" isn't the end |
| 24–30 | The wider estate: other build tools, old Java, scanner reports and risk acceptance, a zero-day emergency, container images, very old services |

---

## Check yourself

Answer from memory before you open the answers: the effort of remembering is what makes it stick.

1. A customer can see someone else's account by changing a number in the web address. Which status code should the service have sent?
2. Why does an old Java version matter even if the service still works?
3. What does "red on purpose" mean?

<details><summary>Answers</summary>

1. **404 Not Found.** Day 15 chooses 404 over 403 ("not yours") on purpose: 403 would confirm the account exists.
2. Unsupported versions stop receiving security fixes, so new weaknesses found in them stay open.
3. A test or a Bruno check that fails on the starter by design. Making it pass is the day's task.

</details>

Anything unclear? Ask in the day's chat. Questions from outside the development team are some of the most useful ones.
