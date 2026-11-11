# Decision record: <finding id>

Copy this file to `reports/decisions/<finding id>.md` (for example `reports/decisions/SAST-999.md`)
for every finding you do **not** fix in code: a false positive or an accepted risk.
Fill in every field. A reviewer must be able to understand the decision without asking you.

| Field | Value |
|---|---|
| Finding id | <e.g. SAST-999, from the report's `findingId`> |
| Rule | <the report's `ruleId`, e.g. `java/example-rule`> |
| Location | <file:line from the report> |
| Decision | <False positive / Accept risk> |
| Justification | <Why. For a false positive: why the code is safe, with evidence (a test, a link to the docs). For an accepted risk: what could go wrong, how likely, what limits the damage today, and the plan to fix it.> |
| Owner | <a named person or role who answers for this decision> |
| Review date | <YYYY-MM-DD: when this is looked at again. Accepted risks expire; they are not forever.> |
