"""Proves every claim course.json makes, day by day.

    python3 tools/course/verify.py                   # everything: files, hand-offs, builds, tests, Bruno
    python3 tools/course/verify.py --day 19 --day 20 # some days
    python3 tools/course/verify.py --structure-only  # seconds: files, wrapper versions, hand-offs, collections

A claim is one sentence the course tells learners or facilitators, e.g. "Day 19's starter fails exactly
TransferTest.aFailedTransferChangesNothing" or "Day 21's collection passes 15/15 on the solution".
The build tool and the running app sit behind a runner, so the checks are testable with a fake one.
"""
import argparse
import glob
import importlib.util
import json
import os
import re
import shutil
import signal
import socket
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET
from collections import namedtuple

sys.path.insert(0, os.path.dirname(__file__))
from course import ROOT, Manifest  # noqa: E402

TestRun = namedtuple("TestRun", "total failing")       # failing: sorted "Class.method" names
BrunoRun = namedtuple("BrunoRun", "total failing")     # failing: sorted request names, e.g. "02 Second"
Finding = namedtuple("Finding", "day claim ok detail")

IGNORED_DIRS = {"build", ".gradle", "target", "data", "bin", "node_modules", ".idea"}
IGNORED_FILES = {".DS_Store"}
HANDOUTS = ("brief.docx", "hints.md", "afternoon-walkthrough.md")


# ---------- pure helpers ----------

def test_report_dirs(project):
    """Where a build left its JUnit XML reports: Gradle's in the root and every subproject, or Maven's Surefire."""
    return sorted(glob.glob(os.path.join(project, "**", "build", "test-results", "test"), recursive=True)
                  + glob.glob(os.path.join(project, "**", "target", "surefire-reports"), recursive=True))


def parse_junit_results(*results_dirs):
    """Reads JUnit XML reports (Gradle's or Maven Surefire's: the format is the same) into a TestRun."""
    total, failing = 0, []
    for path in [p for d in results_dirs for p in glob.glob(os.path.join(d, "*.xml"))]:
        for case in ET.parse(path).getroot().iter("testcase"):
            total += 1
            if case.find("failure") is not None or case.find("error") is not None:
                failing.append(case.get("classname").split(".")[-1] + "." + case.get("name").split("(")[0])
    return TestRun(total, sorted(failing))


def _files(root):
    found = {}
    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in IGNORED_DIRS]
        for name in filenames:
            if name not in IGNORED_FILES:
                path = os.path.join(dirpath, name)
                found[os.path.relpath(path, root)] = path
    return found


def handoff_changes(previous_solution, starter):
    """Relative paths that differ between yesterday's solution and today's starter (added, removed or changed)."""
    before, after = _files(previous_solution), _files(starter)
    changed = set(before) ^ set(after)
    for rel in set(before) & set(after):
        with open(before[rel], "rb") as a, open(after[rel], "rb") as b:
            if a.read() != b.read():
                changed.add(rel)
    return sorted(changed)


def wrapper_version(project, build="gradle"):
    """The Gradle or Maven version a project's wrapper downloads, or None if it has no such wrapper."""
    path, pattern = {
        "gradle": (os.path.join(project, "gradle", "wrapper", "gradle-wrapper.properties"), r"gradle-([0-9.]+)-(bin|all)\.zip"),
        "maven": (os.path.join(project, ".mvn", "wrapper", "maven-wrapper.properties"), r"apache-maven-([0-9.]+)-bin\.zip"),
    }[build]
    if not os.path.exists(path):
        return None
    with open(path) as f:
        match = re.search(pattern, f.read())
    return match.group(1) if match else None


# ---------- checks ----------

def check_day(manifest, number, runner, root=ROOT, structure_only=False):
    d = manifest.day(number)
    folder = os.path.join(root, d["folder"])
    findings = []

    def claim(name, ok, detail=""):
        findings.append(Finding(number, name, ok, detail))

    if not os.path.isdir(folder):
        claim("folder", False, f"{d['folder']} doesn't exist")
        return findings

    handouts = [*HANDOUTS, "solution"] + (["starter"] if d["starter"] else []) + (["bruno"] if d["bruno"] else [])
    missing = [name for name in handouts if not os.path.exists(os.path.join(folder, name))]
    claim("files", not missing, "missing: " + ", ".join(missing) if missing else "")
    if missing:
        return findings

    projects = {"solution": os.path.join(folder, "solution")}
    if d["starter"]:
        projects["starter"] = os.path.join(folder, "starter")
    build = d.get("build", "gradle")
    versions = {name: wrapper_version(p, build) for name, p in projects.items()}
    # One version for the day, or {"starter": …, "solution": …} on a day whose task is to upgrade the build tool
    expected = d[build] if isinstance(d[build], dict) else {name: d[build] for name in projects}
    wrong = {name: v for name, v in versions.items() if v != expected[name]}
    claim("build tool version", not wrong, f"expected {build} {expected}, found {wrong}" if wrong else "")

    if d["starter"] and d["buildsOn"] is not None:
        previous = os.path.join(root, manifest.day(d["buildsOn"])["folder"], "solution")
        actual = handoff_changes(previous, projects["starter"])
        unplanned = sorted(set(actual) - set(d["handoff"]))
        unmade = sorted(set(d["handoff"]) - set(actual))
        detail = "; ".join(filter(None, [
            "unplanned: " + ", ".join(unplanned) if unplanned else "",
            "listed but identical: " + ", ".join(unmade) if unmade else ""]))
        claim("hand-off", not detail, detail)

    if d["bruno"]:
        collection = os.path.join(folder, "bruno")
        requests = len([f for f in os.listdir(collection) if f.endswith(".bru")])
        claim("bruno: request count", requests == d["bruno"]["requests"],
              f"manifest says {d['bruno']['requests']}, collection has {requests}")
        with open(os.path.join(collection, "bruno.json")) as f:
            name = json.load(f).get("name")
        wanted = manifest.collection_name(number)
        claim("bruno: collection name", name == wanted, f"expected {wanted!r}, found {name!r}")

    if structure_only:
        return findings

    if d["starter"]:
        run = runner.run_tests(projects["starter"], build)
        claim("starter: tests", run.total == d["starter"]["tests"], f"expected {d['starter']['tests']}, ran {run.total}")
        claim("starter: red tests", run.failing == sorted(d["starter"]["failing"]),
              f"expected {sorted(d['starter']['failing'])}, failed {run.failing}")
    run = runner.run_tests(projects["solution"], build)
    claim("solution: tests", run.total == d["solution"]["tests"], f"expected {d['solution']['tests']}, ran {run.total}")
    claim("solution: red tests", run.failing == [], f"failed {run.failing}")

    if d["bruno"]:
        collection = os.path.join(folder, "bruno")
        run = runner.replay_bruno(projects["solution"], collection, build)
        claim("bruno: solution", not run.failing and run.total == d["bruno"]["requests"],
              f"replayed {run.total} of {d['bruno']['requests']}, failed {run.failing}")
        if d["starter"] and d["bruno"].get("starterRuns", True):
            run = runner.replay_bruno(projects["starter"], collection, build)
            claim("bruno: starter", run.failing == sorted(d["bruno"]["failingOnStarter"]),
                  f"expected {sorted(d['bruno']['failingOnStarter'])}, failed {run.failing}")
    return findings


GENERATORS = {
    "docs from course.json": [sys.executable, "tools/course/sync.py", "--check"],
    "afternoon walkthroughs": [sys.executable, "tools/briefs/walkthroughs.py", "--check"],
    "briefs and hints": ["node", "tools/briefs/build.js", "--check"],
}


def check_generated(generators=GENERATORS, cwd=ROOT):
    """Each generator, run in --check mode, must find nothing out of date."""
    findings = []
    for name, argv in generators.items():
        result = subprocess.run(argv, cwd=cwd, capture_output=True, text=True)
        stale = [line.strip() for line in (result.stdout + result.stderr).splitlines()
                 if line.startswith("out of date") or line.lstrip().startswith("Error")]
        findings.append(Finding(None, f"generated: {name}", result.returncode == 0, "; ".join(stale[:5])))
    return findings


# ---------- the real runner: Gradle or Maven, and a running app ----------

def _load_check_bruno():
    spec = importlib.util.spec_from_file_location("check_bruno", os.path.join(ROOT, "tools", "check-bruno.py"))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def _remove(path, attempts=10):
    """Deletes a throwaway copy, retrying while a just-stopped app is still letting go of its files."""
    for _ in range(attempts):
        shutil.rmtree(path, ignore_errors=True)
        if not os.path.exists(path):
            return
        time.sleep(1)


def _free_port():
    with socket.socket() as s:
        s.bind(("localhost", 0))
        return s.getsockname()[1]


class BuildRunner:
    """Runs each project with the build tool course.json names (Gradle or Maven) in a throwaway copy, so the course folders never get
    build/, target/, .gradle/ or data/."""

    def __init__(self):
        self.check_bruno = _load_check_bruno()

    def _copy(self, project):
        work = tempfile.mkdtemp(prefix="course-verify-")
        target = os.path.join(work, "project")
        shutil.copytree(project, target, ignore=shutil.ignore_patterns(*IGNORED_DIRS))
        return work, target

    def run_tests(self, project, build):
        work, copy = self._copy(project)
        try:
            argv = (["./mvnw", "-q", "test", "-Dmaven.test.failure.ignore=true"] if build == "maven"
                    else ["./gradlew", "-q", "test", "--continue"])
            result = subprocess.run(argv, cwd=copy, capture_output=True, text=True, timeout=900)
            run = parse_junit_results(*test_report_dirs(copy))
            if run.total == 0 and result.returncode != 0:
                # No tests ran because the build itself failed: say so, rather than "expected N, ran 0"
                lines = (result.stdout + result.stderr).splitlines()
                reason = next((l.strip() for l in lines if "error:" in l or "What went wrong" in l or "[ERROR]" in l), "build failed")
                return TestRun(0, [f"BUILD FAILED: {reason}"])
            return run
        finally:
            _remove(work)

    def replay_bruno(self, project, collection, build):
        work, copy = self._copy(project)
        port = _free_port()
        # --no-daemon keeps the app in our process group, so stopping the group really stops the app
        # (through the shared Gradle daemon it could outlive us and keep writing to its folder)
        argv = (["./mvnw", "-q", "spring-boot:run", f"-Dspring-boot.run.arguments=--server.port={port}"] if build == "maven"
                else ["./gradlew", "-q", "--no-daemon", "bootRun", f"--args=--server.port={port}"])
        app = subprocess.Popen(argv, cwd=copy,
                               stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, start_new_session=True)
        try:
            self._wait_until_up(port, app)
            results = self.check_bruno.replay(collection, f"http://localhost:{port}")
            failing = sorted(name[:-len(".bru")] for name, _, problems in results if problems)
            return BrunoRun(len(results), failing)
        finally:
            os.killpg(app.pid, signal.SIGTERM)
            app.wait(timeout=60)
            self._wait_until_down(port)
            _remove(work)

    @staticmethod
    def _wait_until_down(port, timeout=60):
        deadline = time.time() + timeout
        while time.time() < deadline:
            with socket.socket() as s:
                if s.connect_ex(("localhost", port)) != 0:
                    return
            time.sleep(1)

    @staticmethod
    def _wait_until_up(port, app, timeout=180):
        deadline = time.time() + timeout
        while time.time() < deadline:
            if app.poll() is not None:
                raise RuntimeError("the app stopped before it answered (run it yourself: ./gradlew bootRun or ./mvnw spring-boot:run)")
            try:
                urllib.request.urlopen(f"http://localhost:{port}/", timeout=2)
                return
            except urllib.error.HTTPError:
                return            # any HTTP answer, even 401/404, means it's up
            except OSError:
                time.sleep(1)
        raise RuntimeError(f"the app didn't answer on port {port} within {timeout}s")


# ---------- command line ----------

def main(argv=None):
    parser = argparse.ArgumentParser(description="Prove every claim in course.json.")
    parser.add_argument("--day", type=int, action="append", help="check only this day (repeatable)")
    parser.add_argument("--structure-only", action="store_true", help="no builds or apps: files, versions, hand-offs, collections")
    args = parser.parse_args(argv)

    manifest = Manifest.load()
    numbers = args.day or [d["day"] for d in manifest.days]
    runner = None if args.structure_only else BuildRunner()
    failed = 0
    # Always checked: it takes seconds, and a stale doc matters whichever days you're checking
    generated = check_generated()
    bad = [f for f in generated if not f.ok]
    failed += len(bad)
    print(f"{'✅' if not bad else '❌'} generated files: {len(generated) - len(bad)}/{len(generated)} up to date", flush=True)
    for f in bad:
        print(f"     ✗ {f.claim}: {f.detail}", flush=True)
    for number in numbers:
        findings = check_day(manifest, number, runner, structure_only=args.structure_only)
        bad = [f for f in findings if not f.ok]
        failed += len(bad)
        print(f"{'✅' if not bad else '❌'} day {number:02d} {manifest.day(number)['folder']}: "
              f"{len(findings) - len(bad)}/{len(findings)} claims hold", flush=True)
        for f in bad:
            print(f"     ✗ {f.claim}: {f.detail}", flush=True)
    print(f"\n{'All claims hold.' if not failed else f'{failed} claim(s) failed.'}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
