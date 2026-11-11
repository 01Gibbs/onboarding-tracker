#!/usr/bin/env bash
# Pre-flight check for the course. Run it from the course root the afternoon before Day 1:
#
#   tools/doctor.sh
#
# It only looks; it never installs or changes anything (the Gradle check runs offline).
# Works under bash, zsh and sh on macOS and Linux.

STARTER="day-01-complete-the-crud-api/starter"
MIN_JAVA=25
MIN_FREE_GB=5
GRADLE_TIMEOUT=120 # seconds

# Always work from the course root (the folder above tools/), wherever we were started from.
cd "$(dirname "$0")/.." || exit 2

failures=0
warnings=0

ok()   { printf '✅ %s\n' "$1"; }
bad()  { printf '❌ %s\n   Fix: %s\n' "$1" "$2"; failures=$((failures + 1)); }
warn() { printf '⚠️  %s\n   Tip: %s\n' "$1" "$2"; warnings=$((warnings + 1)); }

# Run a command with a time limit (macOS has no `timeout`). Returns 124 on timeout.
run_with_timeout() {
    limit=$1
    shift
    "$@" &
    pid=$!
    waited=0
    while kill -0 "$pid" 2>/dev/null; do
        if [ "$waited" -ge "$limit" ]; then
            kill "$pid" 2>/dev/null
            sleep 1
            kill -9 "$pid" 2>/dev/null
            wait "$pid" 2>/dev/null
            return 124
        fi
        sleep 1
        waited=$((waited + 1))
    done
    wait "$pid"
}

echo "Course doctor: $(pwd)"
echo

# 1. Java 25 or newer on the PATH
if command -v java >/dev/null 2>&1; then
    # First line looks like: openjdk version "25.0.5" 2026-10-20 LTS   (or "1.8.0_402" on Java 8)
    version=$(java -version 2>&1 | head -n 1 | sed -E 's/^[^"]*"([^"]+)".*$/\1/')
    major=$(printf '%s' "$version" | sed -E 's/^1\.([0-9]+).*/\1/; s/^([0-9]+).*/\1/')
    case "$major" in
        ''|*[!0-9]*) bad "java -version: couldn't read a version from '$version'" "install a JDK ${MIN_JAVA}+ (e.g. Eclipse Temurin) and put its bin/ on your PATH" ;;
        *)
            if [ "$major" -ge "$MIN_JAVA" ]; then
                ok "java -version: $version (${MIN_JAVA}+ needed)"
            else
                bad "java -version: $version, but the course needs ${MIN_JAVA}+" "install JDK ${MIN_JAVA} and put it first on your PATH (check with: java -version). Older JDKs for the legacy days download by themselves."
            fi ;;
    esac
else
    bad "java: command not found" "install JDK ${MIN_JAVA} (e.g. Eclipse Temurin) and open a new terminal"
fi

# 2. The Gradle wrapper exists and is executable
if [ ! -d "$STARTER" ]; then
    bad "$STARTER: folder not found" "run this script from the course repository (tools/doctor.sh), with the day folders next to tools/"
elif [ ! -f "$STARTER/gradlew" ]; then
    bad "$STARTER/gradlew: no such file" "the wrapper is missing; re-clone or re-download the course"
elif [ ! -x "$STARTER/gradlew" ]; then
    bad "$STARTER/gradlew: not executable" "chmod +x day-*/starter/gradlew day-*/solution/gradlew"
else
    not_exec=$(find . -path ./tools -prune -o -name gradlew -type f ! -perm -u+x -print 2>/dev/null | head -n 3)
    if [ -n "$not_exec" ]; then
        bad "some wrappers aren't executable, e.g. $(printf '%s' "$not_exec" | head -n 1)" "chmod +x day-*/starter/gradlew day-*/solution/gradlew"
    else
        ok "./gradlew exists and is executable ($STARTER and every other day)"
    fi
fi

# 3. Port 8080 is free
if command -v lsof >/dev/null 2>&1; then
    holder=$(lsof -nP -iTCP:8080 -sTCP:LISTEN 2>/dev/null | awk 'NR == 2 { print $1 " (PID " $2 ")" }')
    if [ -z "$holder" ]; then
        ok "port 8080 is free"
    else
        bad "port 8080 is in use by $holder" "stop it (Ctrl+C in its terminal, or: kill <PID>). See: lsof -i :8080"
    fi
elif command -v ss >/dev/null 2>&1; then
    if ss -ltn 2>/dev/null | grep -q '[:.]8080 '; then
        bad "port 8080 is in use" "find it with: ss -ltnp | grep 8080, then stop it"
    else
        ok "port 8080 is free"
    fi
else
    warn "couldn't check port 8080 (no lsof or ss)" "if bootRun says 'Port 8080 was already in use', stop the other app"
fi

# 4. Gradle starts and resolves the project from its cache (offline: nothing is downloaded)
if [ -x "$STARTER/gradlew" ]; then
    log=$(mktemp)
    (cd "$STARTER" && run_with_timeout "$GRADLE_TIMEOUT" ./gradlew -q help --offline) >"$log" 2>&1
    rc=$?
    if [ "$rc" -eq 0 ]; then
        ok "Gradle resolves $STARTER offline (wrapper, plugins and cache are ready)"
    elif [ "$rc" -eq 124 ]; then
        bad "Gradle didn't finish within ${GRADLE_TIMEOUT}s" "run it yourself to see why: cd $STARTER && ./gradlew help --offline"
    else
        reason=$(grep -m 1 -A 2 'What went wrong' "$log" | tail -n 1 | sed 's/^ *//')
        [ -z "$reason" ] && reason=$(grep -m 1 -v '^ *$' "$log")
        bad "Gradle can't resolve the project offline: ${reason:-see the output of the command below}" "while online, once: cd $STARTER && ./gradlew build   (downloads Gradle and the libraries into ~/.gradle)"
    fi
    rm -f "$log"
fi

# 5. Bruno (a warning only: you can also call the API with curl)
if [ -d "/Applications/Bruno.app" ] || [ -d "$HOME/Applications/Bruno.app" ]; then
    ok "Bruno is installed (Bruno.app)"
elif command -v bruno >/dev/null 2>&1 || command -v bru >/dev/null 2>&1; then
    ok "Bruno is installed ($(command -v bruno || command -v bru))"
else
    warn "Bruno not found (no Bruno.app, no bruno/bru on PATH)" "install it (until then, curl works): https://www.usebruno.com/downloads"
fi

# 5b. Every day's Bruno collection is well-formed, so it opens in Bruno (no server needed)
if command -v python3 >/dev/null 2>&1; then
    if out=$(python3 tools/check-bruno.py --validate day-*/bruno 2>&1); then
        ok "every Bruno collection is well-formed ($(ls -d day-*/bruno | wc -l | tr -d ' ') collections)"
    else
        bad "a Bruno collection is malformed: $(printf '%s' "$out" | head -n 1)" "re-copy that day's bruno/ folder from the course repository"
    fi
else
    warn "python3 not found, so Bruno collections weren't checked" "install Python 3, or open one collection in Bruno to check it loads"
fi

# 6. Disk space where Gradle keeps its caches (libraries, Gradle versions, the downloaded JDKs 11/17/21)
gradle_home=${GRADLE_USER_HOME:-$HOME/.gradle}
probe=$gradle_home
[ -d "$probe" ] || probe=$HOME
free_kb=$(df -Pk "$probe" 2>/dev/null | awk 'NR == 2 { print $4 }')
case "$free_kb" in
    ''|*[!0-9]*) warn "couldn't measure free disk space" "make sure about ${MIN_FREE_GB} GB is free for ~/.gradle" ;;
    *)
        free_gb=$((free_kb / 1024 / 1024))
        if [ "$free_gb" -ge "$MIN_FREE_GB" ]; then
            ok "disk: ${free_gb} GB free for $gradle_home (${MIN_FREE_GB} GB needed)"
        else
            bad "disk: only ${free_gb} GB free for $gradle_home, ${MIN_FREE_GB} GB needed" "free some space; old Gradle caches can go: rm -rf ~/.gradle/caches (they download again)"
        fi ;;
esac

echo
if [ "$failures" -eq 0 ]; then
    echo "All checks passed ($warnings warning(s)). You're ready for Day 1."
    exit 0
else
    echo "$failures check(s) failed, $warnings warning(s). Fix the ❌ lines above, then run tools/doctor.sh again."
    exit 1
fi
