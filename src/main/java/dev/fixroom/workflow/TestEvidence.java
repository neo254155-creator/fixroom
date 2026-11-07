package dev.fixroom.workflow;

public record TestEvidence(String capturedAt, int exitCode, int tests, int failures,
                           int errors, int skipped, String logFile) {
    public boolean passed() { return exitCode == 0 && tests > 0 && failures == 0 && errors == 0 && skipped == 0; }
    public boolean reproduced() { return exitCode != 0 && tests > 0 && failures > 0 && errors == 0 && skipped == 0; }
}
