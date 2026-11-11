"""One shape for a manifest day in tests, so fixtures can't drift apart."""


def day(n, **over):
    d = {
        "day": n, "folder": f"day-{n:02d}-topic", "title": f"Title {n}", "codebase": "modern", "tagline": "t", "summary": f"s{n}",
        "plain": f"What day {n} is about, without jargon.",
        "gradle": "9.8.2",
        "glance": {"java": "Java 25", "build": "Gradle 9.8 (use the wrapper)", "framework": "Spring Boot 4.1.2",
                   "database": "H2 (embedded)", "apiClient": "Bruno"},
        "buildsOn": n - 1 if n else None,
        "starter": {"tests": 3, "failing": []} if n else None,
        "solution": {"tests": 4},
        "bruno": None, "handoff": [], "diagram": None,
    }
    d.update(over)
    return d
