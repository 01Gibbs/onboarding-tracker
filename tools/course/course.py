"""The course manifest: every fact about every day, stated once.

course.json (next to this file) lists each day once: its folder, title, stack, which day it builds on,
and the claims the course makes about it (test totals, which tests are red on purpose, Bruno results).
Generators read it to write briefs, walkthroughs and doc tables; tools/course/verify.py proves every claim.
"""
import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
MANIFEST_PATH = os.path.join(os.path.dirname(__file__), "course.json")


class ManifestError(Exception):
    pass


class Manifest:

    def __init__(self, data):
        self.days = data["days"]
        self._by_number = {d["day"]: d for d in self.days}
        self._validate()

    @classmethod
    def load(cls, path=MANIFEST_PATH):
        with open(path) as f:
            return cls(json.load(f))

    def day(self, number):
        return self._by_number[number]

    def collection_name(self, number):
        """The name every day's bruno/bruno.json must have."""
        return f"Day {number:02d} — {self.day(number)['title']}"

    def days_with_red_starter_tests(self):
        return [d["day"] for d in self.days if d["starter"] and d["starter"]["failing"]]

    def days_with_red_starter_collection(self):
        return [d["day"] for d in self.days if d["bruno"] and d["bruno"]["failingOnStarter"]]

    def facts(self, number):
        """The values a template may use for one day, as {"token": value}."""
        d = self.day(number)
        facts = {"day": d["day"], "folder": d["folder"], "title": d["title"], "solution.tests": d["solution"]["tests"]}
        if d["starter"]:
            facts["starter.tests"] = d["starter"]["tests"]
            facts["starter.failing"] = len(d["starter"]["failing"])
        if d["bruno"]:
            facts["bruno.requests"] = d["bruno"]["requests"]
            facts["bruno.failingOnStarter"] = len(d["bruno"]["failingOnStarter"])
        return facts

    def render(self, text, number):
        """Replaces {{token}} with the day's facts. A token the day doesn't have is an error, never a blank."""
        facts = self.facts(number)

        def value(match):
            token = match.group(1)
            if facts.get(token) is None:
                raise ManifestError(f"day {number}: no fact called {{{{{token}}}}}")
            return str(facts[token])

        return re.sub(r"\{\{([a-zA-Z.]+)\}\}", value, text)

    def _validate(self):
        for expected, d in enumerate(self.days):
            n = d["day"]
            if n != expected:
                raise ManifestError(f"days must be numbered 0, 1, 2… without gaps: expected day {expected}, found day {n}")
            if not d["folder"].startswith(f"day-{n:02d}-"):
                raise ManifestError(f"day {n}: folder {d['folder']!r} must start with day-{n:02d}-")
            builds_on = d["buildsOn"]
            if builds_on is not None and not 0 <= builds_on < n:
                raise ManifestError(f"day {n}: buildsOn {builds_on} must be an earlier day")
            if d["codebase"] not in ("modern", "legacy", "estate"):
                raise ManifestError(f"day {n}: codebase must be 'modern' (Boot 4 / Java 25), 'legacy' (the 2022 service) "
                                    "or 'estate' (a separate project from the wider estate)")
            if not d.get("plain", "").strip():
                raise ManifestError(f"day {n}: plain must say in a sentence or two, without jargon, what the day is about")
            build = d.get("build", "gradle")
            if build not in ("gradle", "maven"):
                raise ManifestError(f"day {n}: build must be 'gradle' or 'maven', not {build!r}")
            if not d.get(build):
                raise ManifestError(f"day {n}: a {build} day must state its wrapper's {build} version")
            if build == "maven" and d.get("gradle"):
                raise ManifestError(f"day {n}: a maven day has no gradle version")
            bruno = d["bruno"]
            if bruno and len(bruno["failingOnStarter"]) > bruno["requests"]:
                raise ManifestError(f"day {n}: bruno says more requests fail on the starter than the collection has")
