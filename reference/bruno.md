# Using the Bruno collections

Most days ship a [Bruno](https://www.usebruno.com) collection in `day-NN-…/bruno/`: the checks from that day's brief, as requests with assertions. A collection is a folder of plain-text `.bru` files, so it lives in Git next to the code.

## Open and run

1. Start the app: `cd day-NN-…/starter` (or `solution`), then `./gradlew bootRun`. It listens on port 8080.
2. In Bruno: **Open Collection** → the day's `bruno/` folder (the one containing `bruno.json`).
3. Top right: pick the **Local** environment. It sets `{{baseUrl}}` to `http://localhost:8080`.
4. Right-click the collection → **Run**. Every request shows ✅ or ❌.

Each request's **Assert** tab is the check: `res.status: eq 201` means "this must answer 201 Created". A ❌ names the expectation that failed.

## Red on the starter is normal

Collections describe the **finished** behaviour. On days that add or fix something (1, 2, 3, 10, 15, 16, 19 and Days 20–23), some requests fail against `starter/`. That's the to-do list, and they pass once you're done.

## Requests that change data

Creating, updating, deleting and transferring change the database, so a second run can differ (an account you deleted is gone). To start again: stop the app, `rm -rf data`, start it again.

## Logins

The legacy service (Days 6–13) uses `teller` / `teller-pass`. From Day 14 the modern API has `ada` / `ada-pass` and `grace` / `grace-pass` (customers) and `teller` / `teller-pass` (staff). Requests send them from their **Auth** tab.

## Format

The collections use Bruno's classic `.bru` format, which every Bruno version opens (Bruno 3 can also save collections as YAML). Maintainers can replay a collection without Bruno: `python3 tools/check-bruno.py day-NN-…/bruno`.
