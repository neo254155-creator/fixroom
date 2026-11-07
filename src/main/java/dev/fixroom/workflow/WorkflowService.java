package dev.fixroom.workflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
public class WorkflowService {
    public enum Status { OPEN, REPRODUCED, AWAITING_APPROVAL, APPROVED, VERIFIED }
    public record Event(String at, String action, String detail) {}
    public record Incident(String id, String title, String reporter, Status status,
                           String proposal, String approvedBy, String sourceHash, String protectedHash,
                           TestEvidence before, TestEvidence after, List<Event> timeline) {}

    private final Path project;
    private final Path data;
    private final TestRunner runner;
    private final JsonMapper json = JsonMapper.builder().build();
    private final Map<String, Incident> incidents = new LinkedHashMap<>();

    public WorkflowService(@Value("${fixroom.project-dir:.}") String project,
                           @Value("${fixroom.data-dir:artifacts}") String data, TestRunner runner) throws IOException {
        this.project = Path.of(project).toAbsolutePath().normalize();
        this.data = Path.of(data).toAbsolutePath().normalize();
        this.runner = runner;
        Files.createDirectories(this.data);
        Path state = this.data.resolve("incidents.json");
        if (Files.exists(state)) {
            for (Incident incident : json.readValue(Files.readString(state), Incident[].class)) {
                incidents.put(incident.id(), incident);
            }
        }
    }

    public synchronized List<Incident> list() { return List.copyOf(incidents.values()); }
    public synchronized Incident get(String id) {
        Incident found = incidents.get(id);
        if (found == null) throw new NoSuchElementException("Incident not found: " + id);
        return found;
    }

    public synchronized Incident open(String title, String reporter) throws Exception {
        String id = "FIX-" + String.format("%03d", incidents.size() + 1);
        return commit(new Incident(id, title, reporter, Status.OPEN, "", "", sourceHash(), protectedHash(),
                null, null, List.of(event("OPEN", reporter + ": " + title))));
    }

    public synchronized Incident reproduce(String id) throws Exception {
        Incident previous = require(id, Status.OPEN);
        unchanged(previous);
        TestEvidence evidence = runner.run(project, data.resolve(id).resolve("before.log"));
        unchanged(previous);
        if (!evidence.reproduced()) throw new IllegalStateException("Expected an assertion failure in the checkout tests; inspect " + evidence.logFile());
        return transition(previous, Status.REPRODUCED, "", "", evidence, null,
                "REPRODUCED", counts(evidence));
    }

    public synchronized Incident propose(String id, String summary) throws Exception {
        Incident previous = require(id, Status.REPRODUCED);
        unchanged(previous);
        return transition(previous, Status.AWAITING_APPROVAL, summary, "", previous.before(), null,
                "PROPOSED", summary);
    }

    public synchronized Incident approve(String id, String by, String confirmation) throws Exception {
        Incident previous = require(id, Status.AWAITING_APPROVAL);
        if (!("APPROVE " + id).equals(confirmation)) throw new IllegalArgumentException("Confirmation must be APPROVE " + id);
        unchanged(previous);
        return transition(previous, Status.APPROVED, previous.proposal(), by, previous.before(), null,
                "APPROVED", "Approval recorded for " + by + "; source was unchanged at approval time");
    }

    public synchronized Incident verify(String id) throws Exception {
        Incident previous = require(id, Status.APPROVED);
        if (sourceHash().equals(previous.sourceHash())) throw new IllegalStateException("Checkout source has not changed since reproduction");
        if (!protectedHash().equals(previous.protectedHash())) throw new IllegalStateException("Tests or workflow/build files changed; verification refused");
        String fixedHash = sourceHash();
        TestEvidence evidence = runner.run(project, data.resolve(id).resolve("after.log"));
        if (!sourceHash().equals(fixedHash)) throw new IllegalStateException("Checkout source changed during verification; run verification again");
        if (!protectedHash().equals(previous.protectedHash())) throw new IllegalStateException("Protected files changed during verification");
        if (!evidence.passed() || evidence.tests() != previous.before().tests()) {
            throw new IllegalStateException("Verification did not pass the original complete test suite; inspect " + evidence.logFile());
        }
        Incident verified = transition(previous, Status.VERIFIED, previous.proposal(), previous.approvedBy(),
                previous.before(), evidence, "VERIFIED", counts(evidence));
        Files.writeString(data.resolve(id).resolve("resolution.md"), report(id), StandardCharsets.UTF_8);
        return verified;
    }

    public synchronized String report(String id) {
        Incident i = get(id);
        StringBuilder text = new StringBuilder("# " + i.id() + " — " + i.title() + "\n\n");
        text.append("Status: ").append(i.status()).append("\nReporter: ").append(i.reporter())
                .append("\nApproval recorded for: ").append(i.approvedBy().isBlank() ? "Pending" : i.approvedBy())
                .append("\n\n## Proposed fix\n\n").append(i.proposal()).append("\n\n## Test evidence\n\n")
                .append("Before: ").append(i.before() == null ? "Not run" : counts(i.before()))
                .append("\nAfter: ").append(i.after() == null ? "Not run" : counts(i.after()))
                .append("\n\nChanged source: src/main/java/dev/fixroom/checkout/CheckoutService.java\n")
                .append("\n## Timeline\n\n");
        for (Event event : i.timeline()) text.append("- ").append(event.at()).append(" ").append(event.action()).append(": ").append(event.detail()).append("\n");
        text.append("\nApproval names are supplied by the caller; this local demo does not authenticate Discord identities.\n");
        return text.toString();
    }

    private Incident require(String id, Status status) {
        Incident i = get(id);
        if (i.status() != status) throw new IllegalStateException("Expected " + status + ", found " + i.status());
        return i;
    }
    private void unchanged(Incident i) throws Exception {
        if (!sourceHash().equals(i.sourceHash()) || !protectedHash().equals(i.protectedHash())) {
            throw new IllegalStateException("Source, tests or workflow/build files changed before approval; restore them and use a new incident");
        }
    }
    private Incident transition(Incident i, Status status, String proposal, String approvedBy,
                                TestEvidence before, TestEvidence after, String action, String detail) throws IOException {
        List<Event> events = new ArrayList<>(i.timeline());
        events.add(event(action, detail));
        return commit(new Incident(i.id(), i.title(), i.reporter(), status, proposal, approvedBy,
                i.sourceHash(), i.protectedHash(), before, after, List.copyOf(events)));
    }
    private Incident commit(Incident incident) throws IOException {
        Map<String, Incident> next = new LinkedHashMap<>(incidents);
        next.put(incident.id(), incident);
        Path temporary = data.resolve("incidents.json.tmp");
        Files.writeString(temporary, json.writerWithDefaultPrettyPrinter().writeValueAsString(next.values()));
        try { Files.move(temporary, data.resolve("incidents.json"), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException e) { Files.move(temporary, data.resolve("incidents.json"), StandardCopyOption.REPLACE_EXISTING); }
        incidents.put(incident.id(), incident);
        return incident;
    }
    private String sourceHash() throws Exception { return hash(List.of(project.resolve("src/main/java/dev/fixroom/checkout/CheckoutService.java"))); }
    private String protectedHash() throws Exception {
        List<Path> files = new ArrayList<>(List.of(project.resolve("pom.xml"), project.resolve("mvnw"), project.resolve("mvnw.cmd")));
        for (String folder : List.of("src/test", "src/main/java/dev/fixroom/workflow", ".mvn")) {
            try (var stream = Files.walk(project.resolve(folder))) { stream.filter(Files::isRegularFile).forEach(files::add); }
        }
        files.sort(Comparator.comparing(Path::toString));
        return hash(files);
    }
    private String hash(List<Path> files) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        for (Path file : files) {
            digest.update(project.relativize(file).toString().getBytes(StandardCharsets.UTF_8));
            digest.update((byte) 0);
            digest.update(Files.readAllBytes(file));
            digest.update((byte) 0);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    private static Event event(String action, String detail) { return new Event(Instant.now().toString(), action, detail); }
    private static String counts(TestEvidence e) { return e.tests() + " tests; " + e.failures() + " failures; " + e.errors() + " errors; " + e.skipped() + " skipped; exit " + e.exitCode(); }
}
