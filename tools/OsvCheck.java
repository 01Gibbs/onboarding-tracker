import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tiny, no-install vulnerability check for a Gradle lockfile, using the free OSV API (https://osv.dev).
 *
 * Usage (from a day's starter/ folder, after ./gradlew dependencies --write-locks):
 *   java ../../tools/OsvCheck.java gradle.lockfile
 *
 * Only libraries on the runtimeClasspath (what you actually ship) are checked.
 * This is a teaching tool: it reads JSON with regular expressions instead of a real parser.
 * In CI, use a real scanner such as OSV-Scanner or OWASP Dependency-Check.
 */
public class OsvCheck {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final List<String> SEVERITIES = List.of("CRITICAL", "HIGH", "MODERATE", "LOW", "UNKNOWN");

    record Library(String group, String artifact, String version) {
        String name() {
            return group + ":" + artifact;
        }
    }

    record Advisory(String id, String cve, String severity, String summary) {
    }

    public static void main(String[] args) throws Exception {
        Path lockfile = Path.of(args.length > 0 ? args[0] : "gradle.lockfile");
        if (!Files.exists(lockfile)) {
            System.err.println("No lockfile at " + lockfile.toAbsolutePath());
            System.err.println("Create one first with: ./gradlew dependencies --write-locks");
            System.err.println("Usage: java ../../tools/OsvCheck.java gradle.lockfile");
            System.exit(1);
        }
        List<Library> libraries = readRuntimeLibraries(lockfile);
        if (libraries.isEmpty()) {
            System.err.println("No runtimeClasspath libraries found in " + lockfile + ". Is it a Gradle lockfile?");
            System.exit(1);
        }

        List<List<String>> idsPerLibrary;
        try {
            idsPerLibrary = queryBatch(libraries);
        } catch (Exception e) {
            System.err.println("Could not reach the OSV API (https://api.osv.dev): " + e.getMessage());
            System.err.println("Check your internet connection, or look libraries up by hand at https://osv.dev");
            System.exit(1);
            return;
        }

        int vulnerableLibraries = 0;
        int advisories = 0;
        Map<String, Integer> bySeverity = new LinkedHashMap<>();
        SEVERITIES.forEach(severity -> bySeverity.put(severity, 0));
        for (int i = 0; i < libraries.size(); i++) {
            List<String> ids = idsPerLibrary.get(i);
            if (ids.isEmpty()) {
                continue;
            }
            vulnerableLibraries++;
            advisories += ids.size();
            Library library = libraries.get(i);
            System.out.printf("%n%s %s  (%d advisories)%n", library.name(), library.version(), ids.size());
            for (Advisory advisory : fetchDetails(ids)) {
                bySeverity.merge(advisory.severity(), 1, Integer::sum);
                System.out.printf("  %-9s %-20s %-16s %s%n",
                        advisory.severity(), advisory.id(), advisory.cve(), advisory.summary());
            }
        }
        System.out.printf("%nChecked %d runtime libraries: %d vulnerable, %d advisories.%n",
                libraries.size(), vulnerableLibraries, advisories);
        System.out.println("By severity: " + bySeverity);
        System.out.println("Look any ID up at https://osv.dev/vulnerability/<ID>");
    }

    // Lockfile lines look like: org.yaml:snakeyaml:1.30=compileClasspath,runtimeClasspath
    static List<Library> readRuntimeLibraries(Path lockfile) throws Exception {
        List<Library> libraries = new ArrayList<>();
        for (String line : Files.readAllLines(lockfile)) {
            if (line.startsWith("#") || !line.contains("=") || !line.contains("runtimeClasspath")) {
                continue;
            }
            String[] coordinates = line.substring(0, line.indexOf('=')).split(":");
            if (coordinates.length < 3) {
                continue; // e.g. the "empty=..." line at the end of the file
            }
            libraries.add(new Library(coordinates[0], coordinates[1], coordinates[2]));
        }
        return libraries;
    }

    // One request for all libraries: https://google.github.io/osv.dev/post-v1-querybatch/
    static List<List<String>> queryBatch(List<Library> libraries) throws Exception {
        StringBuilder body = new StringBuilder("{\"queries\":[");
        for (int i = 0; i < libraries.size(); i++) {
            Library library = libraries.get(i);
            body.append(i == 0 ? "" : ",")
                    .append("{\"package\":{\"ecosystem\":\"Maven\",\"name\":\"").append(library.name())
                    .append("\"},\"version\":\"").append(library.version()).append("\"}");
        }
        body.append("]}");

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.osv.dev/v1/querybatch"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        if (response.statusCode() != 200 || json.indexOf('[') < 0) {
            throw new IllegalStateException("HTTP " + response.statusCode() + ": "
                    + json.substring(0, Math.min(200, json.length())));
        }

        // The response has one {...} result per query, in the same order as the queries.
        List<List<String>> idsPerLibrary = new ArrayList<>();
        String results = json.substring(json.indexOf('[') + 1, json.lastIndexOf(']'));
        for (String result : splitTopLevelObjects(results)) {
            List<String> ids = new ArrayList<>();
            Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(result);
            while (matcher.find()) {
                ids.add(matcher.group(1));
            }
            idsPerLibrary.add(ids);
        }
        if (idsPerLibrary.size() != libraries.size()) {
            throw new IllegalStateException("expected " + libraries.size() + " results but got " + idsPerLibrary.size());
        }
        return idsPerLibrary;
    }

    static List<Advisory> fetchDetails(List<String> ids) {
        List<CompletableFuture<Advisory>> futures = new ArrayList<>();
        for (String id : ids) {
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.osv.dev/v1/vulns/" + id)).build();
            futures.add(HTTP.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> response.statusCode() == 200
                            ? toAdvisory(id, response.body())
                            : unknown(id))
                    .exceptionally(error -> unknown(id)));
        }
        return futures.stream()
                .map(CompletableFuture::join)
                .sorted(Comparator.comparingInt(advisory -> SEVERITIES.indexOf(advisory.severity())))
                .toList();
    }

    // Used when the details for one advisory can't be fetched: the rest of the report still prints.
    static Advisory unknown(String id) {
        return new Advisory(id, "-", "UNKNOWN", "(could not fetch details: see https://osv.dev/vulnerability/" + id + ")");
    }

    static Advisory toAdvisory(String id, String json) {
        // The CVE number is listed in "aliases", e.g. "aliases": ["CVE-2022-1471"]
        Set<String> cves = new LinkedHashSet<>();
        Matcher cve = Pattern.compile("CVE-\\d{4}-\\d+").matcher(find(json, "\"aliases\"\\s*:\\s*\\[([^\\]]*)\\]", ""));
        while (cve.find()) {
            cves.add(cve.group());
        }
        String severity = find(json, "\"severity\"\\s*:\\s*\"([A-Za-z]+)\"", "UNKNOWN").toUpperCase();
        if (severity.equals("MEDIUM")) {
            severity = "MODERATE";
        }
        if (!SEVERITIES.contains(severity)) {
            severity = "UNKNOWN";
        }
        String summary = find(json, "\"summary\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"", "(no summary)");
        if (summary.length() > 70) {
            summary = summary.substring(0, 67) + "...";
        }
        return new Advisory(id, cves.isEmpty() ? "-" : cves.iterator().next(), severity, summary);
    }

    static String find(String json, String regex, String fallback) {
        Matcher matcher = Pattern.compile(regex).matcher(json);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    static List<String> splitTopLevelObjects(String array) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;
        for (int i = 0; i < array.length(); i++) {
            char c = array.charAt(i);
            if (escaped) {
                escaped = false; // this character was escaped, e.g. the quote in \"
            } else if (inString && c == '\\') {
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
            } else if (!inString && c == '{') {
                if (depth++ == 0) {
                    start = i;
                }
            } else if (!inString && c == '}') {
                if (--depth == 0) {
                    objects.add(array.substring(start, i + 1));
                }
            }
        }
        return objects;
    }
}
