"""Writes each day's afternoon-walkthrough.md (presenter notes for the 20–30 minute solution session).
Edit walkthroughs.json (the session content) or course.json (folders, titles, counts), then from the course root:
    python3 tools/briefs/walkthroughs.py           # write
    python3 tools/briefs/walkthroughs.py --check   # exit 1 if any file is out of date
"""
import json
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), "..", "course"))
from course import ROOT, Manifest  # noqa: E402

# One entry per day: tour (in the order to show it), points, mistakes, questions, bridge (what to find in the
# team's own code beforehand), tomorrow. The day's folder, title, diagram box and whether it has a Bruno
# collection come from course.json.
with open(os.path.join(os.path.dirname(__file__), "walkthroughs.json")) as f:
    DAYS = json.load(f)


def with_manifest(entry, manifest):
    """Adds the day's facts from course.json, and fills {{tokens}} in the session text."""
    day = manifest.day(entry["day"])
    filled = json.loads(manifest.render(json.dumps(entry, ensure_ascii=False), entry["day"]))
    return {**filled, "folder": day["folder"], "title": day["title"], "box": day["diagram"]["box"],
            "has_bruno": day["bruno"] is not None, "plain": day["plain"], "maven": day.get("build") == "maven",
            "accounts_api": day["codebase"] != "estate",
            "multi_project": _includes_subprojects(os.path.join(ROOT, day["folder"], "solution"))}


def _includes_subprojects(project):
    """A multi-project Gradle build writes one test report per subproject, not one at the root."""
    settings = os.path.join(project, "settings.gradle.kts")
    if not os.path.exists(settings):
        return False
    with open(settings) as f:
        return "include(" in f.read()


def walkthrough_markdown(d):
    lines = [
        f"# Day {d['day']:02d} afternoon — {d['title']}",
        "",
        f"> **In plain English:** {d['plain']}",
        "",
        "**Presenter notes for the 20–30 minute solution walkthrough.** Morning brief: [`brief.docx`](brief.docx). "
        "Have open: `solution/` in the IDE, "
        + ("the Bruno collection in `bruno/`, " if d["has_bruno"]
           else "the test output (`target/surefire-reports/`), " if d["maven"]
           else "each subproject's HTML test report (`<subproject>/build/reports/tests/test/index.html`), " if d["multi_project"]
           else "the HTML test report (`build/reports/tests/test/index.html`), ")
        + "and a terminal in `solution/`.",
        "",
        (f"Where today's change sits in the app: **{d['box']}** (see the [Accounts API diagram](../reference/accounts-api.md))."
         if d["accounts_api"] else f"What changes today: **{d['box']}**."),
        "",
        ("To show everything that changed today, from the course root:" if d["day"] else "There's no starter today (learners generated their own project). The whole solution, from the course root:"),
        "",
        "```bash",
        (f"diff -r -x build -x target -x .gradle {d['folder']}/starter {d['folder']}/solution" if d["day"] else f"ls {d['folder']}/solution/src/main/java/com/example/onboarding"),
        "```",
        "",
        "## Agenda (25 min)",
        "",
        "| Time | What |",
        "|---|---|",
        "| 0–3 | Recap the question. Show of hands: who got it green? Where did people get stuck in the chat today? |",
        "| 3–14 | Solution tour (below), in order |",
        "| 14–19 | Points to land, and common mistakes |",
        "| 19–22 | Questions for the room |",
        "| 22–25 | In our code, then tomorrow's question |",
        "",
        "## Solution tour",
        "",
    ]
    for i, (where, what) in enumerate(d["tour"], 1):
        lines.append(f"{i}. **{where}**: {what}")
    lines += ["", "## Points to land", ""] + [f"- {p}" for p in d["points"]]
    lines += ["", "## Common mistakes", ""] + [f"- {m}" for m in d["mistakes"]]
    lines += ["", "## Questions for the room", ""]
    for q, a in d["questions"]:
        lines += [f"- **{q}**", f"  <details><summary>Answer</summary>{a}</details>"]
    lines += ["", "## In our code (2 minutes)", "",
              f"Prepare before the session: find {d['bridge']} Show it on screen and ask the room what's the same as today's solution, and what's different.",
              "", "## Tomorrow", "", d["tomorrow"], ""]
    return "\n".join(lines)


def main(check=False):
    manifest = Manifest.load()
    stale = []
    for entry in DAYS:
        d = with_manifest(entry, manifest)
        path = os.path.join(ROOT, d["folder"], "afternoon-walkthrough.md")
        text = walkthrough_markdown(d)
        if check:
            current = open(path).read() if os.path.exists(path) else None
            if current != text:
                stale.append(os.path.relpath(path, ROOT))
        else:
            with open(path, "w") as f:
                f.write(text)
            print("wrote", os.path.relpath(path, ROOT))
    for path in stale:
        print("out of date:", path)
    return 1 if stale else 0


if __name__ == "__main__":
    sys.exit(main(check="--check" in sys.argv))
