"""Replays a Bruno (.bru) collection with plain HTTP calls and evaluates its assert blocks.
For maintainers: proves a day's collection passes against its solution without the Bruno app or CLI.

Usage: python3 tools/check-bruno.py <collection-dir> [baseUrl]
       python3 tools/check-bruno.py --validate <collection-dir>...   (structure only, no server needed)
"""
import base64, json, os, re, sys, urllib.error, urllib.request

def blocks(text):
    return {m.group(1): m.group(2) for m in re.finditer(r"^([a-z:-]+) \{\n(.*?)^\}", text, re.S | re.M)}

def pairs(block):
    out = []
    for line in block.splitlines():
        line = line.strip()
        if line and ":" in line:
            k, v = line.split(":", 1)
            out.append((k.strip(), v.strip()))
    return out

def lookup(obj, path):
    for part in re.findall(r"[^.\[\]]+", path):
        if isinstance(obj, list):
            if int(part) >= len(obj):
                return "__undefined__"
            obj = obj[int(part)]
        elif isinstance(obj, dict) and part in obj:
            obj = obj[part]
        else:
            return "__undefined__"
    return obj

def literal(v):
    v = v.strip()
    if len(v) >= 2 and v[0] == v[-1] and v[0] in "\"'":
        return v[1:-1]
    try:
        return json.loads(v)
    except ValueError:
        return v

def text_of(x):
    return x if isinstance(x, str) else json.dumps(x)

OPS = {
    "eq": lambda a, w: a == w or str(a) == str(w) or (isinstance(a, (int, float)) and isinstance(w, (int, float)) and float(a) == float(w)),
    "neq": lambda a, w: str(a) != str(w),
    "isDefined": lambda a, w: a not in ("__undefined__", None),
    "isUndefined": lambda a, w: a == "__undefined__",
    "isArray": lambda a, w: isinstance(a, list),
    "isEmpty": lambda a, w: len(a) == 0,
    "contains": lambda a, w: str(w) in text_of(a),
    "startsWith": lambda a, w: str(a).startswith(str(w)),
    "isNumber": lambda a, w: isinstance(a, (int, float)),
}

def check(coldir, base=None):
    """Replays the collection and prints a PASS/FAIL line per request. Returns the number that failed."""
    results = replay(coldir, base)
    for name, status, problems in results:
        print(f"  {'PASS' if not problems else 'FAIL'} {name} [{status}] " + "; ".join(problems))
    failures = sum(1 for _, _, problems in results if problems)
    print(f"{coldir}: {len(results) - failures}/{len(results)} passed")
    return failures


def replay(coldir, base=None):
    """Sends every request in order and evaluates its asserts. Returns [(file name, status, problems)]."""
    env = dict(pairs(blocks(open(os.path.join(coldir, "environments", "Local.bru")).read())["vars"]))
    if base:
        env["baseUrl"] = base
    runtime, results = {}, []
    files = sorted(f for f in os.listdir(coldir) if f.endswith(".bru"))
    for name in files:
        b = blocks(open(os.path.join(coldir, name)).read())
        method = next(m for m in ("get", "post", "put", "delete", "patch") if m in b)
        req = dict(pairs(b[method]))
        sub = lambda s: re.sub(r"\{\{(\w+)\}\}", lambda m: str({**env, **runtime}.get(m.group(1), m.group(0))), s)
        headers, data = {}, None
        if req.get("auth") == "basic":
            a = dict(pairs(b["auth:basic"]))
            headers["Authorization"] = "Basic " + base64.b64encode(f"{a['username']}:{a['password']}".encode()).decode()
        for k, v in pairs(b.get("headers", "")):
            headers[k] = sub(v)
        if req.get("body") == "json":
            data, headers["Content-Type"] = sub(b["body:json"]).encode(), "application/json"
        try:
            resp = urllib.request.urlopen(urllib.request.Request(sub(req["url"]), data=data, method=method.upper(), headers=headers))
        except urllib.error.HTTPError as e:
            resp = e
        status, raw = resp.status, resp.read().decode()
        try:
            body = json.loads(raw)
        except ValueError:
            body = raw
        hdrs = {k.lower(): v for k, v in resp.headers.items()}
        problems = []
        for key, expr in pairs(b.get("assert", "")):
            op, _, val = expr.partition(" ")
            if key == "res.status":
                actual = status
            elif key.startswith("res.body"):
                actual = body if key == "res.body" else lookup(body, key[len("res.body"):])
            elif key.startswith("res.headers."):
                actual = hdrs.get(key[len("res.headers."):], "__undefined__")
            else:
                problems.append(f"unsupported {key}"); continue
            if op not in OPS:
                problems.append(f"unknown op {op}")
            elif not OPS[op](actual, literal(val) if val else None):
                problems.append(f"{key} {expr} (got {str(actual)[:60]})")
        for var, field in re.findall(r'bru\.setVar\("(\w+)",\s*res\.body\.(\w+)\)', b.get("script:post-response", "")):
            runtime[var] = body.get(field) if isinstance(body, dict) else None
        results.append((name, status, problems))
    return results

def validate(coldir):
    """Checks a collection opens in Bruno: bruno.json, a Local environment, and well-formed requests."""
    problems = []
    try:
        meta = json.load(open(os.path.join(coldir, "bruno.json")))
        if meta.get("type") != "collection":
            problems.append("bruno.json: type is not 'collection'")
    except (OSError, ValueError) as e:
        problems.append(f"bruno.json: {e}")
    env = os.path.join(coldir, "environments", "Local.bru")
    if not os.path.exists(env) or "baseUrl" not in open(env).read():
        problems.append("environments/Local.bru: missing, or no baseUrl")
    for name in sorted(f for f in os.listdir(coldir) if f.endswith(".bru")):
        text = open(os.path.join(coldir, name)).read()
        if text.count("{") != text.count("}"):
            problems.append(f"{name}: unbalanced braces")
        b = blocks(text)
        if "meta" not in b or not any(m in b for m in ("get", "post", "put", "delete", "patch")):
            problems.append(f"{name}: needs a meta block and a request block")
        if "assert" not in b:
            problems.append(f"{name}: no assert block")
    return problems


if __name__ == "__main__":
    if sys.argv[1] == "--validate":
        bad = 0
        for coldir in sys.argv[2:]:
            for p in validate(coldir):
                bad += 1
                print(f"{coldir}: {p}")
        sys.exit(1 if bad else 0)
    sys.exit(1 if check(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None) else 0)
