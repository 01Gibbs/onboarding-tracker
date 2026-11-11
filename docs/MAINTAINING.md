# Maintaining the course

Everything about each day is stated once, in `tools/course/course.json`: folder, title, tagline, its **In plain English** summary (`plain`, shown first in the brief and the walkthrough), stack and build tool (`build`: `gradle` by default, or `maven` with a `maven` version), which day it builds on, test totals, the exact tests and Bruno requests that are red on the starter, the diagram box, and which files may differ from the previous day's solution. Everything that restates those facts is generated from it, and one command proves the whole course still works.

## Change something

| To change… | Edit | Then run |
|---|---|---|
| A day's facts (title, folder, plain-English summary, counts, red tests…) | `tools/course/course.json` | all three generators below |
| A brief's task text | `tools/briefs/briefs.js` | `cd tools/briefs && npm install && node build.js` |
| Recall, "Done when", new terms, hints | `tools/briefs/extras.js` | `cd tools/briefs && node build.js` |
| An afternoon walkthrough | `tools/briefs/walkthroughs.json` | `python3 tools/briefs/walkthroughs.py` |
| README table, red-on-purpose lists, diagram table, Bruno names | (generated) | `python3 tools/course/sync.py` |

**Never type a count or a day title into prose.** Use a `{{token}}` instead (for example `{{bruno.requests}}` or `{{solution.tests}}`; the full list is `facts()` in `tools/course/course.py`). Day 0's brief is the team's original `.docx`, and its `hints.md` is hand-written.

## Prove it

```bash
python3 tools/course/verify.py                   # everything, ~30 minutes: builds, tests, Bruno on starter and solution
python3 tools/course/verify.py --structure-only  # seconds: files, wrapper versions, hand-offs, collections, generated files
python3 tools/course/verify.py --day 19          # one day
cd tools/briefs && npm test                      # unit tests + the fast verifier
```

To replay one collection against an app you started yourself: `python3 tools/check-bruno.py day-NN-…/bruno`.

A starter that can't run at all (it isn't a runnable app until the day's work is done) sets `"starterRuns": false` under `bruno`, and the verifier replays the collection against the solution only.

## Red on purpose

Don't "fix" these, they're the day's task: <!-- course:red -->starter tests red on purpose on Days 10, 16, 19–30; Bruno requests that fail on the starter until the work is done on Days 1–3, 10, 14–16, 19, 21–23, 25<!-- /course:red -->.

## Gotchas

See [NOTES.md](NOTES.md) ("Code gotchas"): the legacy days must stay on Gradle 8.14.6, OpenRewrite is pinned, advisory counts are a dated snapshot, and more.

## Other documents here

- [MISSION.md](MISSION.md): why the course exists and what success looks like
- [NOTES.md](NOTES.md): the delivery model and teaching preferences
- [RESOURCES.md](RESOURCES.md): the primary sources behind the briefs
