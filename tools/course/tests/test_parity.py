import json
import shutil
import subprocess
import unittest

from course import ROOT, Manifest


@unittest.skipUnless(shutil.which("node"), "node is needed to load the brief builder")
class FactsParityTest(unittest.TestCase):
    """build.js (JavaScript) and course.py (Python) both fill {{tokens}}; they must agree on every day."""

    def test_both_languages_give_the_same_facts_for_every_day(self):
        script = ("const { facts } = require('./tools/briefs/build.js');"
                  "const days = require('./tools/course/course.json').days;"
                  "console.log(JSON.stringify(Object.fromEntries(days.map((d) => [d.day, facts(d.day)]))));")
        out = subprocess.run(["node", "-e", script], cwd=ROOT, capture_output=True, text=True, check=True).stdout
        from_js = {int(k): v for k, v in json.loads(out.splitlines()[-1]).items()}
        manifest = Manifest.load()
        for d in manifest.days:
            with self.subTest(day=d["day"]):
                self.assertEqual(from_js[d["day"]], manifest.facts(d["day"]))


if __name__ == "__main__":
    unittest.main()
