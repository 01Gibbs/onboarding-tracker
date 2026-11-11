import json
import os
import shutil
import tempfile
import textwrap
import unittest

from course import Manifest
from tests.fixtures import day
from verify import TestRun, BrunoRun, check_day, check_generated, handoff_changes, parse_junit_results, test_report_dirs


class FakeRunner:
    """Stands in for Gradle and a running app: answers from canned results keyed by project folder."""

    def __init__(self, tests=None, bruno=None):
        self.tests, self.bruno = tests or {}, bruno or {}
        self.builds = []

    def run_tests(self, project, build="gradle"):
        self.builds.append(build)
        return self.tests[os.path.basename(os.path.dirname(project)) + "/" + os.path.basename(project)]

    def replay_bruno(self, project, collection, build="gradle"):
        return self.bruno[os.path.basename(os.path.dirname(project)) + "/" + os.path.basename(project)]


def write(path, text=""):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        f.write(text)


def make_project(root, gradle="9.8.2", files=None, maven=None):
    if maven:
        write(f"{root}/.mvn/wrapper/maven-wrapper.properties",
              f"distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/{maven}/apache-maven-{maven}-bin.zip\n")
    else:
        write(f"{root}/gradle/wrapper/gradle-wrapper.properties",
              f"distributionUrl=https\\://services.gradle.org/distributions/gradle-{gradle}-bin.zip\n")
    for rel, text in (files or {}).items():
        write(f"{root}/{rel}", text)


class CourseFixture(unittest.TestCase):
    """Two days on disk: day 0 (solution only) and day 1 (starter, solution, a 2-request collection)."""

    def setUp(self):
        self.root = tempfile.mkdtemp()
        make_project(f"{self.root}/day-00-a/solution", files={"src/A.java": "a"})
        make_project(f"{self.root}/day-01-b/starter", files={"src/A.java": "a", "src/ATest.java": "todo"})
        make_project(f"{self.root}/day-01-b/solution", files={"src/A.java": "a2", "src/ATest.java": "test"})
        for d in ("day-00-a", "day-01-b"):
            write(f"{self.root}/{d}/brief.docx")
            write(f"{self.root}/{d}/hints.md")
            write(f"{self.root}/{d}/afternoon-walkthrough.md")
        write(f"{self.root}/day-01-b/bruno/bruno.json", json.dumps({"version": "1", "name": "Day 01 — B", "type": "collection"}))
        write(f"{self.root}/day-01-b/bruno/01 First.bru")
        write(f"{self.root}/day-01-b/bruno/02 Second.bru")
        self.manifest_data = {"days": [
            day(0, folder="day-00-a", title="A", solution={"tests": 1}),
            day(1, folder="day-01-b", title="B", starter={"tests": 2, "failing": ["ATest.isRed"]}, solution={"tests": 3},
                bruno={"requests": 2, "failingOnStarter": ["02 Second"]}, handoff=["src/ATest.java"]),
        ]}
        self.good_runner = FakeRunner(
            tests={"day-00-a/solution": TestRun(1, []), "day-01-b/starter": TestRun(2, ["ATest.isRed"]),
                   "day-01-b/solution": TestRun(3, [])},
            bruno={"day-01-b/solution": BrunoRun(2, []), "day-01-b/starter": BrunoRun(2, ["02 Second"])})

    def failures(self, number, runner=None, manifest_data=None):
        m = Manifest(manifest_data or self.manifest_data)
        return [f for f in check_day(m, number, runner or self.good_runner, self.root) if not f.ok]


class CheckDayTest(CourseFixture):

    def test_a_day_whose_claims_all_hold_has_no_failures(self):
        self.assertEqual(self.failures(1), [])

    def test_a_wrong_test_total_is_reported(self):
        runner = self.good_runner
        runner.tests["day-01-b/solution"] = TestRun(4, [])
        self.assertIn("solution: tests", [f.claim for f in self.failures(1, runner)])

    def test_a_starter_red_on_a_different_test_is_reported(self):
        runner = self.good_runner
        runner.tests["day-01-b/starter"] = TestRun(2, ["ATest.somethingElse"])
        self.assertIn("starter: red tests", [f.claim for f in self.failures(1, runner)])

    def test_a_red_solution_is_reported(self):
        runner = self.good_runner
        runner.tests["day-01-b/solution"] = TestRun(3, ["ATest.broken"])
        self.assertIn("solution: red tests", [f.claim for f in self.failures(1, runner)])

    def test_a_collection_failing_on_the_solution_is_reported(self):
        runner = self.good_runner
        runner.bruno["day-01-b/solution"] = BrunoRun(2, ["01 First"])
        self.assertIn("bruno: solution", [f.claim for f in self.failures(1, runner)])

    def test_a_collection_failing_differently_on_the_starter_is_reported(self):
        runner = self.good_runner
        runner.bruno["day-01-b/starter"] = BrunoRun(2, [])
        self.assertIn("bruno: starter", [f.claim for f in self.failures(1, runner)])

    def test_an_unplanned_difference_from_the_previous_solution_is_reported(self):
        write(f"{self.root}/day-01-b/starter/src/Extra.java", "surprise")
        found = [f for f in self.failures(1) if f.claim == "hand-off"]
        self.assertEqual(len(found), 1)
        self.assertIn("src/Extra.java", found[0].detail)

    def test_a_replay_that_skipped_requests_is_reported(self):
        runner = self.good_runner
        runner.bruno["day-01-b/solution"] = BrunoRun(1, [])
        self.assertIn("bruno: solution", [f.claim for f in self.failures(1, runner)])

    def test_a_listed_handoff_file_that_did_not_change_is_reported(self):
        self.manifest_data["days"][1]["handoff"] = ["src/ATest.java", "src/A.java"]
        found = [f for f in self.failures(1) if f.claim == "hand-off"]
        self.assertEqual(len(found), 1)
        self.assertIn("listed but identical: src/A.java", found[0].detail)

    def test_a_request_count_that_does_not_match_the_files_is_reported(self):
        write(f"{self.root}/day-01-b/bruno/03 Third.bru")
        self.assertIn("bruno: request count", [f.claim for f in self.failures(1)])

    def test_a_collection_name_that_does_not_match_the_title_is_reported(self):
        self.manifest_data["days"][1]["title"] = "Renamed"
        self.assertIn("bruno: collection name", [f.claim for f in self.failures(1)])

    def test_a_wrong_wrapper_version_is_reported(self):
        self.manifest_data["days"][1]["gradle"] = "8.14.6"
        self.assertIn("build tool version", [f.claim for f in self.failures(1)])

    def test_a_maven_day_checks_the_maven_wrapper_version(self):
        for p in ("starter", "solution"):
            make_project(f"{self.root}/day-01-b/{p}-m", maven="3.9.11")
            shutil.rmtree(f"{self.root}/day-01-b/{p}")
            os.rename(f"{self.root}/day-01-b/{p}-m", f"{self.root}/day-01-b/{p}")
        self.manifest_data["days"][1].update(build="maven", maven="3.9.11", gradle=None, buildsOn=None, handoff=[])
        self.assertNotIn("build tool version", [f.claim for f in self.failures(1)])
        self.assertEqual(set(self.good_runner.builds), {"maven"})   # the manifest, not a file on disk, picks the tool
        self.manifest_data["days"][1]["maven"] = "3.9.9"
        self.assertIn("build tool version", [f.claim for f in self.failures(1)])

    def test_an_upgrade_day_can_state_a_different_wrapper_version_for_starter_and_solution(self):
        make_project(f"{self.root}/day-01-b/solution", gradle="9.9.0")
        self.manifest_data["days"][1]["gradle"] = {"starter": "9.8.2", "solution": "9.9.0"}
        self.assertNotIn("build tool version", [f.claim for f in self.failures(1)])
        self.manifest_data["days"][1]["gradle"] = {"starter": "9.8.2", "solution": "9.8.2"}
        self.assertIn("build tool version", [f.claim for f in self.failures(1)])

    def test_a_project_without_its_wrapper_is_reported(self):
        os.remove(f"{self.root}/day-01-b/starter/gradle/wrapper/gradle-wrapper.properties")
        self.assertIn("build tool version", [f.claim for f in self.failures(1)])

    def test_a_starter_that_cannot_start_is_not_replayed(self):
        self.manifest_data["days"][1]["bruno"]["starterRuns"] = False
        self.manifest_data["days"][1]["bruno"]["failingOnStarter"] = []
        del self.good_runner.bruno["day-01-b/starter"]          # replaying it would raise KeyError
        self.assertEqual(self.failures(1), [])

    def test_a_missing_handout_file_is_reported(self):
        os.remove(f"{self.root}/day-01-b/hints.md")
        self.assertIn("files", [f.claim for f in self.failures(1)])

    def test_structure_only_mode_runs_no_builds(self):
        class Exploding:
            def run_tests(self, project, build): raise AssertionError("built a project")
            def replay_bruno(self, project, collection, build): raise AssertionError("started an app")
        m = Manifest(self.manifest_data)
        self.assertTrue(all(f.ok for f in check_day(m, 1, Exploding(), self.root, structure_only=True)))


class CheckGeneratedTest(unittest.TestCase):

    def test_a_generator_that_crashes_reports_its_error_line(self):
        findings = check_generated({"crashes": ["sh", "-c", "echo noise; echo 'Error: no fact called {{x}}'; exit 1"]}, cwd=".")
        self.assertFalse(findings[0].ok)
        self.assertEqual(findings[0].detail, "Error: no fact called {{x}}")

    def test_each_generator_that_reports_stale_files_is_a_failed_claim(self):
        findings = check_generated({"fresh": ["true"], "stale": ["sh", "-c", "echo out of date: x; exit 1"]}, cwd=".")
        self.assertEqual([(f.claim, f.ok) for f in findings], [("generated: fresh", True), ("generated: stale", False)])
        self.assertIn("out of date: x", findings[1].detail)


class HandoffChangesTest(unittest.TestCase):

    def test_lists_files_added_removed_or_changed_ignoring_build_output(self):
        a, b = tempfile.mkdtemp(), tempfile.mkdtemp()
        write(f"{a}/same.txt", "x"); write(f"{b}/same.txt", "x")
        write(f"{a}/changed.txt", "1"); write(f"{b}/changed.txt", "2")
        write(f"{a}/removed.txt", "r")
        write(f"{b}/added.txt", "n")
        write(f"{b}/build/out.class", "ignored"); write(f"{b}/data/db.mv.db", "ignored")
        self.assertEqual(handoff_changes(a, b), ["added.txt", "changed.txt", "removed.txt"])


class ParseJunitResultsTest(unittest.TestCase):

    def test_counts_tests_and_names_the_failing_ones(self):
        d = tempfile.mkdtemp()
        write(f"{d}/TEST-com.example.ATest.xml", textwrap.dedent("""\
            <testsuite name="com.example.ATest" tests="3">
              <testcase classname="com.example.ATest" name="passes()"/>
              <testcase classname="com.example.ATest" name="fails()"><failure message="x"/></testcase>
              <testcase classname="com.example.ATest" name="errors(CapturedOutput)"><error message="y"/></testcase>
            </testsuite>"""))
        self.assertEqual(parse_junit_results(d), TestRun(3, ["ATest.errors", "ATest.fails"]))


class TestReportDirsTest(unittest.TestCase):

    def test_finds_gradle_reports_in_every_subproject_and_maven_reports(self):
        d = tempfile.mkdtemp()
        for rel in ("build/test-results/test", "services/payments/build/test-results/test", "target/surefire-reports",
                    "build/test-results/other"):
            os.makedirs(f"{d}/{rel}")
        self.assertEqual(sorted(os.path.relpath(p, d) for p in test_report_dirs(d)),
                         ["build/test-results/test", "services/payments/build/test-results/test", "target/surefire-reports"])

    def test_a_surefire_report_counts_like_a_gradle_one(self):
        d = tempfile.mkdtemp()
        write(f"{d}/TEST-com.example.BTest.xml", textwrap.dedent("""\
            <testsuite name="com.example.BTest" tests="2">
              <testcase name="passes" classname="com.example.BTest"/>
              <testcase name="fails" classname="com.example.BTest"><failure/></testcase>
            </testsuite>"""))
        self.assertEqual(parse_junit_results(d), TestRun(2, ["BTest.fails"]))


if __name__ == "__main__":
    unittest.main()
