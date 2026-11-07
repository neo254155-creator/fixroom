package dev.fixroom.workflow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Queue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class WorkflowServiceTest {
    @TempDir Path root;
    private Path project;
    private Path data;
    private FakeRunner runner;
    private WorkflowService workflow;

    static class FakeRunner extends TestRunner {
        final Queue<TestEvidence> results = new ArrayDeque<>();
        @Override public TestEvidence run(Path project, Path log) throws Exception {
            Files.createDirectories(log.getParent());
            return results.remove();
        }
    }

    @BeforeEach void setup() throws Exception {
        project = root.resolve("project");
        data = root.resolve("data");
        for (String folder : new String[]{"src/test", "src/main/java/dev/fixroom/workflow", ".mvn", "src/main/java/dev/fixroom/checkout"}) {
            Files.createDirectories(project.resolve(folder));
        }
        for (String file : new String[]{"pom.xml", "mvnw", "mvnw.cmd", "src/test/check.txt", "src/main/java/dev/fixroom/checkout/CheckoutService.java"}) {
            Files.writeString(project.resolve(file), "original");
        }
        runner = new FakeRunner();
        workflow = new WorkflowService(project.toString(), data.toString(), runner);
    }

    private String awaitingApproval() throws Exception {
        var incident = workflow.open("Coupon total incorrect", "NEOPAT");
        runner.results.add(new TestEvidence("before", 1, 8, 3, 0, 0, "before.log"));
        workflow.reproduce(incident.id());
        workflow.propose(incident.id(), "Correct percentage conversion");
        return incident.id();
    }
    private void fixSource() throws Exception { Files.writeString(project.resolve("src/main/java/dev/fixroom/checkout/CheckoutService.java"), "fixed"); }

    @Test void cannotVerifyWithoutApproval() throws Exception {
        String id = awaitingApproval();
        assertThrows(IllegalStateException.class, () -> workflow.verify(id));
    }
    @Test void rejectsIncorrectConfirmation() throws Exception {
        String id = awaitingApproval();
        assertThrows(IllegalArgumentException.class, () -> workflow.approve(id, "NEOPAT", "yes"));
        assertEquals(WorkflowService.Status.AWAITING_APPROVAL, workflow.get(id).status());
    }
    @Test void detectsSourceEditedBeforeApproval() throws Exception {
        String id = awaitingApproval();
        fixSource();
        assertThrows(IllegalStateException.class, () -> workflow.approve(id, "NEOPAT", "APPROVE " + id));
    }
    @Test void rejectsTamperedTests() throws Exception {
        String id = awaitingApproval();
        workflow.approve(id, "NEOPAT", "APPROVE " + id);
        fixSource();
        Files.writeString(project.resolve("src/test/check.txt"), "weakened");
        assertThrows(IllegalStateException.class, () -> workflow.verify(id));
    }
    @Test void recordsSuccessfulWorkflowAndSurvivesRestart() throws Exception {
        String id = awaitingApproval();
        workflow.approve(id, "NEOPAT", "APPROVE " + id);
        fixSource();
        runner.results.add(new TestEvidence("after", 0, 8, 0, 0, 0, "after.log"));
        var verified = workflow.verify(id);
        assertEquals(WorkflowService.Status.VERIFIED, verified.status());
        assertEquals(5, verified.timeline().size());
        assertTrue(Files.readString(data.resolve(id).resolve("resolution.md")).contains("8 tests; 0 failures"));
        var restored = new WorkflowService(project.toString(), data.toString(), runner);
        assertEquals(verified, restored.get(id));
    }
    @Test void failingVerificationKeepsApprovedState() throws Exception {
        String id = awaitingApproval();
        workflow.approve(id, "NEOPAT", "APPROVE " + id);
        fixSource();
        runner.results.add(new TestEvidence("after", 1, 8, 1, 0, 0, "after.log"));
        assertThrows(IllegalStateException.class, () -> workflow.verify(id));
        assertEquals(WorkflowService.Status.APPROVED, workflow.get(id).status());
    }
    @Test void skippedTestsCannotCloseIncident() throws Exception {
        String id = awaitingApproval();
        workflow.approve(id, "NEOPAT", "APPROVE " + id);
        fixSource();
        runner.results.add(new TestEvidence("after", 0, 8, 0, 0, 3, "after.log"));
        assertThrows(IllegalStateException.class, () -> workflow.verify(id));
    }
    @Test void passingBaselineCannotCountAsReproduction() throws Exception {
        String id = workflow.open("Coupon", "NEOPAT").id();
        runner.results.add(new TestEvidence("before", 0, 8, 0, 0, 0, "before.log"));
        assertThrows(IllegalStateException.class, () -> workflow.reproduce(id));
        assertEquals(WorkflowService.Status.OPEN, workflow.get(id).status());
    }
}
