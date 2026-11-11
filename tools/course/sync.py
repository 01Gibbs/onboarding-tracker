"""Writes the parts of the docs that restate course.json, so they can't drift from it.

    python3 tools/course/sync.py          # write
    python3 tools/course/sync.py --check  # exit 1 if anything is out of date

What it owns (each between <!-- course:NAME --> markers, or the whole file):
- README.md: the days table (course:days), the day count (course:count) and which days use which codebase
  (course:modern-days, course:legacy-days, course:estate-days); HOW-TO-USE.md: the legacy days (course:legacy-days)
- docs/MAINTAINING.md, docs/NOTES.md and reference/troubleshooting.md: the red-on-purpose list (course:red)
- reference/accounts-api.md: the "which day changes which box" table (course:diagram)
- day-NN-…/bruno/bruno.json: the collection name, "Day NN — Title"
"""
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(__file__))
from course import ROOT, Manifest  # noqa: E402

MARKED_DOCS = {"README.md": ["days", "count", "modern-days", "legacy-days", "estate-days"], "HOW-TO-USE.md": ["legacy-days"],
               "docs/MAINTAINING.md": ["red"], "docs/NOTES.md": ["red"], "reference/troubleshooting.md": ["red"],
               "reference/accounts-api.md": ["diagram"]}


class SyncError(Exception):
    pass


def replace_between(text, name, content):
    start, end = f"<!-- course:{name} -->", f"<!-- /course:{name} -->"
    pattern = re.compile(re.escape(start) + r".*?" + re.escape(end), re.S)
    if not pattern.search(text):
        raise SyncError(f"markers {start} … {end} not found")
    return pattern.sub(lambda _: start + content + end, text, count=1)


def _ranges(numbers):
    """[1, 2, 3, 5] -> "1–3, 5"."""
    parts, start = [], None
    for i, n in enumerate(numbers):
        if start is None:
            start = n
        if i + 1 == len(numbers) or numbers[i + 1] != n + 1:
            parts.append(str(start) if start == n else f"{start}–{n}")
            start = None
    return ", ".join(parts)


def _days(numbers):
    if not numbers:
        return "no days"
    return ("Day " if len(numbers) == 1 else "Days ") + _ranges(numbers)


def red_summary(manifest):
    return (f"starter tests red on purpose on {_days(manifest.days_with_red_starter_tests())}; "
            f"Bruno requests that fail on the starter until the work is done on {_days(manifest.days_with_red_starter_collection())}")


def codebase_days(manifest, codebase):
    """Which days use one codebase, e.g. "Days 6–13" for the 2022 service."""
    return _days([d["day"] for d in manifest.days if d["codebase"] == codebase])


def days_table(manifest):
    rows = ["| Day | Workshop | You'll practise | Builds on |", "|---|---|---|---|"]
    for d in manifest.days:
        builds_on = d.get("startsFrom") or f"Day {d['buildsOn']}"
        rows.append(f"| {d['day']} | [{d['title']}]({d['folder']}) | {d['summary']} | {builds_on} |")
    return "\n".join(rows)


def diagram_table(manifest):
    rows = ["| Day | Box | What changes | Why |", "|---|---|---|---|"]
    for d in manifest.days:
        g = d.get("diagram") or {}
        if g.get("change"):                    # only days that change the app's structure get a row
            rows.append(f"| [{d['day']}](../{d['folder']}/) | {g['box']} | {g['change']} | {g['why']} |")
    return "\n".join(rows)


def planned_files(manifest, root=ROOT):
    """{path: content} for every file this module owns."""
    files = {}
    for rel, names in MARKED_DOCS.items():
        path = os.path.join(root, rel)
        text = open(path).read()
        for name in names:
            content = {"days": lambda: "\n" + days_table(manifest) + "\n",
                       "diagram": lambda: "\n" + diagram_table(manifest) + "\n",
                       "red": lambda: red_summary(manifest),
                       "count": lambda: str(len(manifest.days)),
                       "modern-days": lambda: codebase_days(manifest, "modern"),
                       "legacy-days": lambda: codebase_days(manifest, "legacy"),
                       "estate-days": lambda: codebase_days(manifest, "estate")}[name]()
            text = replace_between(text, name, content)
        files[path] = text
    for d in manifest.days:
        path = os.path.join(root, d["folder"], "bruno", "bruno.json")
        if d["bruno"]:
            data = json.load(open(path))
            data["name"] = manifest.collection_name(d["day"])
            files[path] = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
    return files


def main(check=False):
    stale = []
    for path, content in planned_files(Manifest.load()).items():
        current = open(path).read() if os.path.exists(path) else None
        if current == content:
            continue
        if check:
            stale.append(os.path.relpath(path, ROOT))
        else:
            with open(path, "w") as f:
                f.write(content)
            print("wrote", os.path.relpath(path, ROOT))
    for path in stale:
        print("out of date:", path)
    return 1 if stale else 0


if __name__ == "__main__":
    sys.exit(main(check="--check" in sys.argv))
