package dev.fixroom.workflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;

@Component
public class TestRunner {
    public TestEvidence run(Path project, Path log) throws Exception {
        Path report = project.resolve("target/surefire-reports/TEST-dev.fixroom.checkout.CheckoutServiceTest.xml");
        // Remove this one known report so an earlier run cannot be mistaken for new evidence.
        Files.deleteIfExists(report);
        Files.createDirectories(log.getParent());
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        List<String> command = windows
                ? List.of("cmd.exe", "/d", "/c", "mvnw.cmd", "-B", "-Dstyle.color=never", "-Dtest=CheckoutServiceTest", "test")
                : List.of("sh", "./mvnw", "-B", "-Dstyle.color=never", "-Dtest=CheckoutServiceTest", "test");
        ProcessBuilder builder = new ProcessBuilder(command).directory(project.toFile())
                .redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().put("JAVA_HOME", System.getProperty("java.home"));
        Process process = builder.start();
        if (!process.waitFor(180, TimeUnit.SECONDS)) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
            throw new IOException("Tests exceeded 180 seconds; inspect " + log);
        }
        if (!Files.isRegularFile(report)) {
            throw new IOException("No new JUnit report was produced. Inspect " + log + " for build or dependency errors.");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        var suite = factory.newDocumentBuilder().parse(report.toFile()).getDocumentElement();
        return new TestEvidence(Instant.now().toString(), process.exitValue(),
                Integer.parseInt(suite.getAttribute("tests")), Integer.parseInt(suite.getAttribute("failures")),
                Integer.parseInt(suite.getAttribute("errors")), Integer.parseInt(suite.getAttribute("skipped")),
                log.toString());
    }
}
