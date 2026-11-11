import unittest

from course import Manifest
from sync import days_table, red_summary, replace_between, SyncError
from tests.fixtures import day


class ReplaceBetweenTest(unittest.TestCase):

    def test_replaces_only_the_text_between_the_markers(self):
        text = "before <!-- course:x -->old<!-- /course:x --> after"
        self.assertEqual(replace_between(text, "x", "new"), "before <!-- course:x -->new<!-- /course:x --> after")

    def test_missing_markers_fail_loudly(self):
        with self.assertRaisesRegex(SyncError, "course:x"):
            replace_between("no markers here", "x", "new")


class RedSummaryTest(unittest.TestCase):

    def test_names_red_test_days_and_red_collection_days_with_ranges(self):
        m = Manifest({"days": [
            day(0), day(1, bruno={"requests": 2, "failingOnStarter": ["01 a"]}),
            day(2, starter={"tests": 1, "failing": ["A.b"]}, bruno={"requests": 1, "failingOnStarter": ["01 a"]}),
            day(3, starter={"tests": 1, "failing": ["A.c"]}), day(4, starter={"tests": 1, "failing": ["A.d"]}),
        ]})
        self.assertEqual(red_summary(m),
                         "starter tests red on purpose on Days 2–4; Bruno requests that fail on the starter until the work is done on Days 1–2")


class RedSummaryWithNothingRedTest(unittest.TestCase):

    def test_says_none_rather_than_an_empty_list(self):
        m = Manifest({"days": [day(0), day(1)]})
        self.assertEqual(red_summary(m),
                         "starter tests red on purpose on no days; Bruno requests that fail on the starter until the work is done on no days")


class DiagramTableTest(unittest.TestCase):

    def test_lists_only_days_that_change_the_diagram(self):
        from sync import diagram_table
        m = Manifest({"days": [day(0, diagram={"box": "Controller"}), day(1, diagram={"box": "Controller", "change": "c", "why": "w"})]})
        rows = diagram_table(m).splitlines()
        self.assertEqual(len(rows), 3)
        self.assertEqual(rows[2], "| [1](../day-01-topic/) | Controller | c | w |")


class CodebaseDaysTest(unittest.TestCase):

    def test_ranges_of_days_per_codebase(self):
        from sync import codebase_days
        m = Manifest({"days": [day(0, codebase="modern"), day(1, codebase="legacy"), day(2, codebase="legacy"),
                               day(3, codebase="modern")]})
        self.assertEqual(codebase_days(m, "modern"), "Days 0, 3")
        self.assertEqual(codebase_days(m, "legacy"), "Days 1–2")


class DaysTableTest(unittest.TestCase):

    def test_one_row_per_day_linking_its_folder(self):
        m = Manifest({"days": [day(0, startsFrom="—"), day(1)]})
        rows = days_table(m).splitlines()
        self.assertEqual(rows[2], "| 0 | [Title 0](day-00-topic) | s0 | — |")
        self.assertEqual(rows[3], "| 1 | [Title 1](day-01-topic) | s1 | Day 0 |")


if __name__ == "__main__":
    unittest.main()
