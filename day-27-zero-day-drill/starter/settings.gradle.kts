// The team's estate in one build: three services and the shared libraries they use.
rootProject.name = "estate"

include("libs:audit")          // shared audit logging, written by the platform team in 2021
include("libs:reporting")      // shared statement formatting; uses libs:audit
include("services:payments")
include("services:statements")
include("services:notifications")
