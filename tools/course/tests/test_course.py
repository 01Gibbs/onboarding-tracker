import unittest

from course import Manifest, ManifestError
from tests.fixtures import day


class LoadingTest(unittest.TestCase):

    def test_a_valid_manifest_gives_each_day_by_number(self):
        m = Manifest({"days": [day(0), day(1)]})
        self.assertEqual(m.day(1)["folder"], "day-01-topic")
        self.assertEqual([d["day"] for d in m.days], [0, 1])

    def test_days_must_be_numbered_without_gaps(self):
        with self.assertRaisesRegex(ManifestError, "day 2"):
            Manifest({"days": [day(0), day(1), day(3)]})

    def test_a_folder_must_start_with_its_day_number(self):
        with self.assertRaisesRegex(ManifestError, "folder"):
            Manifest({"days": [day(0), day(1, folder="day-07-topic")]})

    def test_a_day_can_only_build_on_an_earlier_day(self):
        with self.assertRaisesRegex(ManifestError, "buildsOn"):
            Manifest({"days": [day(0), day(1, buildsOn=1)]})

    def test_a_codebase_must_be_modern_legacy_or_estate(self):
        with self.assertRaisesRegex(ManifestError, "codebase"):
            Manifest({"days": [day(0, codebase="old")]})

    def test_a_codebase_may_be_a_separate_project_from_the_wider_estate(self):
        Manifest({"days": [day(0, codebase="estate")]})

    def test_a_build_tool_must_be_gradle_or_maven(self):
        with self.assertRaisesRegex(ManifestError, "build"):
            Manifest({"days": [day(0, build="ant")]})

    def test_a_gradle_day_states_its_gradle_version(self):
        with self.assertRaisesRegex(ManifestError, "gradle"):
            Manifest({"days": [day(0, gradle=None)]})

    def test_a_maven_day_states_its_maven_version_and_no_gradle_version(self):
        with self.assertRaisesRegex(ManifestError, "maven"):
            Manifest({"days": [day(0, build="maven", gradle=None)]})
        with self.assertRaisesRegex(ManifestError, "gradle"):
            Manifest({"days": [day(0, build="maven", maven="3.9.11")]})

    def test_every_day_explains_itself_in_plain_english(self):
        with self.assertRaisesRegex(ManifestError, "plain"):
            Manifest({"days": [day(0, plain="")]})

    def test_a_collection_cannot_fail_more_requests_than_it_has(self):
        with self.assertRaisesRegex(ManifestError, "bruno"):
            Manifest({"days": [day(0, bruno={"requests": 1, "failingOnStarter": ["01 a", "02 b"]})]})


class CollectionNameTest(unittest.TestCase):

    def test_is_the_day_number_and_title(self):
        self.assertEqual(Manifest({"days": [day(0)]}).collection_name(0), "Day 00 — Title 0")


class QuestionsTest(unittest.TestCase):

    def setUp(self):
        self.m = Manifest({"days": [
            day(0),
            day(1, bruno={"requests": 5, "failingOnStarter": ["01 a", "02 b"]}),
            day(2, starter={"tests": 3, "failing": ["XTest.isRed"]}),
        ]})

    def test_days_whose_starter_tests_are_red_on_purpose(self):
        self.assertEqual(self.m.days_with_red_starter_tests(), [2])

    def test_days_whose_collection_fails_on_the_starter(self):
        self.assertEqual(self.m.days_with_red_starter_collection(), [1])


class RenderingTest(unittest.TestCase):

    def setUp(self):
        self.m = Manifest({"days": [day(0), day(1, bruno={"requests": 5, "failingOnStarter": ["01 a", "02 b"]})]})

    def test_tokens_are_replaced_with_the_day_facts(self):
        text = "All {{bruno.requests}} must pass; {{bruno.failingOnStarter}} fail first; {{solution.tests}} tests in {{folder}}."
        self.assertEqual(self.m.render(text, 1), "All 5 must pass; 2 fail first; 4 tests in day-01-topic.")

    def test_an_unknown_token_fails_loudly(self):
        with self.assertRaisesRegex(ManifestError, "bruno.request"):
            self.m.render("{{bruno.request}}", 1)

    def test_a_fact_with_no_value_yet_fails_loudly_rather_than_printing_none(self):
        m = Manifest({"days": [day(0, solution={"tests": None})]})
        with self.assertRaisesRegex(ManifestError, "solution.tests"):
            m.render("{{solution.tests}}", 0)

    def test_a_token_for_a_fact_the_day_does_not_have_fails_loudly(self):
        with self.assertRaisesRegex(ManifestError, "bruno.requests"):
            self.m.render("{{bruno.requests}}", 0)


if __name__ == "__main__":
    unittest.main()
