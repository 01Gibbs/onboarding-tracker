package com.example.accounts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

// Checks the paperwork against the code: every finding in the scanner report has a decision in TRIAGE.md,
// a finding decided "Fix" really is fixed in the code, a "Duplicate" points at another finding on the same
// line, and every finding we don't fix in code has a written record someone owns.
class TriageTest {

    static final Path REPORT = Path.of("reports/sast-report.sarif");
    static final Path TRIAGE = Path.of("TRIAGE.md");
    static final Path DECISIONS = Path.of("reports/decisions");
    static final Set<String> DECISION_VALUES = Set.of("Fix", "False positive", "Accept risk", "Duplicate");

    record Row(String findingId, String decision, String justification) {
    }

    // Where the report says the problem is: the file, the line and the code it flagged there
    record Finding(String id, String uri, int line, String snippet) {
    }

    @Test
    void everyFindingHasADecision() throws IOException {
        Map<String, Row> rows = triageRows();

        List<Finding> findings = findings();
        for (Finding finding : findings) {
            String id = finding.id();
            assertThat(rows).as("TRIAGE.md has a row for %s", id).containsKey(id);
            Row row = rows.get(id);
            assertThat(row.decision()).as("decision for %s", id).isIn(DECISION_VALUES);
            assertThat(row.justification()).as("justification for %s", id).isNotBlank();
            if (row.decision().equals("Duplicate")) {
                // A duplicate is the same problem in the same place: its justification names that other finding
                List<String> sameLine = findings.stream()
                        .filter(other -> !other.id().equals(id))
                        .filter(other -> other.uri().equals(finding.uri()) && other.line() == finding.line())
                        .map(Finding::id)
                        .toList();
                assertThat(sameLine)
                        .as("%s is a Duplicate, so its justification must name another finding reported at %s:%d",
                                id, finding.uri(), finding.line())
                        .anyMatch(row.justification()::contains);
            }
        }
    }

    @Test
    void everyFixRemovedTheFlaggedCode() throws IOException {
        Map<String, Row> rows = triageRows();
        for (Finding finding : findings()) {
            Row row = rows.get(finding.id());
            if (row == null || !row.decision().equals("Fix")) {
                continue;
            }
            // Spaces and line breaks don't count, so reformatting the line doesn't hide it
            Path file = Path.of(finding.uri());
            String code = Files.exists(file) ? withoutWhitespace(Files.readString(file)) : "";
            assertThat(code)
                    .as("%s is decided Fix, but the code the report flagged is still in %s: %s",
                            finding.id(), finding.uri(), finding.snippet())
                    .doesNotContain(withoutWhitespace(finding.snippet()));
        }
    }

    @Test
    void everyDecisionNotFixedInCodeHasARecord() throws IOException {
        for (Row row : triageRows().values()) {
            if (!row.decision().equals("False positive") && !row.decision().equals("Accept risk")) {
                continue;
            }
            Path record = DECISIONS.resolve(row.findingId() + ".md");
            assertThat(record).as("a decision record for %s", row.findingId()).exists();
            Map<String, String> fields = tableFields(Files.readString(record));
            assertThat(fields.get("Decision")).as("Decision in %s", record).isEqualTo(row.decision());
            assertThat(fields.get("Justification")).as("Justification in %s", record).isNotBlank().doesNotStartWith("<");
            assertThat(fields.get("Owner")).as("Owner in %s", record).isNotBlank().doesNotStartWith("<");
            assertThat(fields.get("Review date")).as("Review date in %s", record).matches("\\d{4}-\\d{2}-\\d{2}");
        }
    }

    // Each result in the SARIF report carries our scanner's finding id in its property bag, and one location
    // with the flagged code as its snippet
    static List<Finding> findings() throws IOException {
        JsonNode sarif = JsonMapper.builder().build().readTree(Files.readString(REPORT));
        List<Finding> findings = new ArrayList<>();
        for (JsonNode run : sarif.get("runs")) {
            for (JsonNode result : run.get("results")) {
                JsonNode location = result.path("locations").path(0).path("physicalLocation");
                findings.add(new Finding(
                        result.path("properties").path("findingId").asString(),
                        location.path("artifactLocation").path("uri").asString(),
                        location.path("region").path("startLine").asInt(),
                        location.path("region").path("snippet").path("text").asString()));
            }
        }
        return findings;
    }

    static String withoutWhitespace(String text) {
        return text.replaceAll("\\s+", "");
    }

    // | Finding | Rule | Location | Decision | Justification |
    static Map<String, Row> triageRows() throws IOException {
        Map<String, Row> rows = new LinkedHashMap<>();
        for (String line : Files.readAllLines(TRIAGE)) {
            List<String> cells = cells(line);
            if (cells.size() >= 5 && cells.get(0).matches("SAST-\\d+")) {
                rows.put(cells.get(0), new Row(cells.get(0), cells.get(3), cells.get(4)));
            }
        }
        return rows;
    }

    // | Field | Value |
    static Map<String, String> tableFields(String markdown) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (String line : markdown.split("\n")) {
            List<String> cells = cells(line);
            if (cells.size() >= 2) {
                fields.put(cells.get(0), cells.get(1));
            }
        }
        return fields;
    }

    static List<String> cells(String line) {
        String trimmed = line.trim();
        if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
            return List.of();
        }
        List<String> cells = new ArrayList<>();
        for (String cell : trimmed.substring(1, trimmed.length() - 1).split("\\|", -1)) {
            cells.add(cell.trim());
        }
        return cells;
    }
}
