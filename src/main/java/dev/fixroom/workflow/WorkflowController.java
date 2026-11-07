package dev.fixroom.workflow;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class WorkflowController {
    private final WorkflowService workflow;
    public WorkflowController(WorkflowService workflow) { this.workflow = workflow; }
    public record OpenRequest(@NotBlank @Size(max=200) String title, @NotBlank @Size(max=80) String reporter) {}
    public record ProposalRequest(@NotBlank @Size(max=2000) String summary) {}
    public record ApprovalRequest(@NotBlank @Size(max=80) String approvedBy, @NotBlank String confirmation) {}

    @GetMapping("/health") public Map<String, String> health() { return Map.of("status", "UP", "project", "FixRoom", "stack", "Spring Boot"); }
    @GetMapping("/incidents") public List<WorkflowService.Incident> list() { return workflow.list(); }
    @GetMapping("/incidents/{id}") public WorkflowService.Incident get(@PathVariable String id) { return workflow.get(id); }
    @PostMapping("/incidents") @ResponseStatus(HttpStatus.CREATED)
    public WorkflowService.Incident open(@Valid @RequestBody OpenRequest body) throws Exception { return workflow.open(body.title(), body.reporter()); }
    @PostMapping("/incidents/{id}/reproduce")
    public WorkflowService.Incident reproduce(@PathVariable String id) throws Exception { return workflow.reproduce(id); }
    @PostMapping("/incidents/{id}/propose")
    public WorkflowService.Incident propose(@PathVariable String id, @Valid @RequestBody ProposalRequest body) throws Exception { return workflow.propose(id, body.summary()); }
    @PostMapping("/incidents/{id}/approve")
    public WorkflowService.Incident approve(@PathVariable String id, @Valid @RequestBody ApprovalRequest body) throws Exception { return workflow.approve(id, body.approvedBy(), body.confirmation()); }
    @PostMapping("/incidents/{id}/verify")
    public WorkflowService.Incident verify(@PathVariable String id) throws Exception { return workflow.verify(id); }
    @GetMapping(value="/incidents/{id}/report", produces=MediaType.TEXT_PLAIN_VALUE)
    public String report(@PathVariable String id) { return workflow.report(id); }

    @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,String> missing(NoSuchElementException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IllegalStateException.class) @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String,String> conflict(IllegalStateException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("error", e.getMessage()); }
    @ExceptionHandler(IOException.class) @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Map<String,String> buildFailure(IOException e) { return Map.of("error", e.getMessage()); }
}
