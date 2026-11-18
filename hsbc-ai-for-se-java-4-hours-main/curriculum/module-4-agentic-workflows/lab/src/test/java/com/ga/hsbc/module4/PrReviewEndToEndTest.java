package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The whole PR-review flow with a scripted model: a final check once your agent loop AND your gate are done.
 * The model "proposes" the reference fix (read from ../solutions); nothing changes on disk until a human approves.
 */
class PrReviewEndToEndTest {
    @TempDir Path tmp;
    Path repo;
    static final Path FIXED = Path.of("..", "solutions", "BusinessDays.fixed.java");

    @BeforeEach void copyRepo() throws IOException {
        assumeTrue(Files.exists(FIXED), "solutions/ is not present in this copy of the repository");
        repo = tmp.resolve("repo");
        Path src = Path.of("sample-service");
        try (Stream<Path> s = Files.walk(src)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                Path dest = repo.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(dest); else Files.copy(p, dest);
            }
        }
    }

    private String read(String rel) throws IOException { return Files.readString(repo.resolve(rel), StandardCharsets.UTF_8); }

    // ---------- end to end with a scripted model ----------
    private List<String> script() throws IOException {
        String fixed = Files.readString(FIXED, StandardCharsets.UTF_8);
        return List.of(
                ScriptedAnthropic.toolUse("t1", "list_files", Map.of()),
                ScriptedAnthropic.toolUse("t2", "read_error_log", Map.of()),
                ScriptedAnthropic.toolUse("t3", "read_file", Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java")),
                ScriptedAnthropic.toolUse("t4", "propose_patch", Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java",
                        "new_content", fixed, "explanation", "previousBusinessDay only skipped Sunday, so Monday resolved to Saturday.")),
                ScriptedAnthropic.finalText("Root cause: BusinessDays.previousBusinessDay lands on Saturday for Monday runs."));
    }

    private ToolAgent.AgentResult runAgent(ScriptedAnthropic api, List<PendingPatch> sink, List<String> approvalsAsked) throws Exception {
        ToolRegistry registry = new ToolRegistry()
                .register(RepoTools.listFiles(repo)).register(RepoTools.readFile(repo))
                .register(RepoTools.readErrorLog(repo)).register(RepoTools.proposePatch(repo, sink));
        ApprovalGate gate = PrReviewApp.makeGate((a, d) -> { approvalsAsked.add(a); return true; });
        return new ToolAgent(api.client(), registry, gate, 10).run("sys", "The overnight job failed. Find the cause.");
    }

    @Test void theAgentProposesAFixAndNothingChangesUntilAHumanApproves() throws Exception {
        String original = read("src/main/java/com/atlas/recon/BusinessDays.java");
        List<PendingPatch> proposals = new ArrayList<>();
        try (ScriptedAnthropic api = new ScriptedAnthropic(script())) {
            ToolAgent.AgentResult r = runAgent(api, proposals, new ArrayList<>());

            assertEquals(5, r.steps());
            assertEquals(1, proposals.size());
            assertEquals(original, read("src/main/java/com/atlas/recon/BusinessDays.java"));   // proposal alone changes nothing
            // the model really was shown the buggy line and the failing log
            String allRequests = MiniJson.toJson(api.requests);
            assertTrue(allRequests.contains("previous.getDayOfWeek() == DayOfWeek.SUNDAY"));
            assertTrue(allRequests.contains("ArithmeticException"));
        }

        PendingPatch patch = proposals.get(0);
        String shown = PatchApplier.diff(repo, patch);
        assertTrue(shown.contains("- ") && shown.contains("+ "));

        assertFalse(PatchApplier.applyIfApproved(repo, patch, PrReviewApp.makeGate((a, d) -> false)));   // the human says no, through the SAME gate
        assertEquals(original, read("src/main/java/com/atlas/recon/BusinessDays.java"));
        assertFalse(Files.exists(repo.resolve("src/main/java/com/atlas/recon/BusinessDays.java.bak")));

        assertTrue(PatchApplier.applyIfApproved(repo, patch, PrReviewApp.makeGate((a, d) -> true)));     // approved
        assertEquals(patch.newContent(), read("src/main/java/com/atlas/recon/BusinessDays.java"));
        assertEquals(original, read("src/main/java/com/atlas/recon/BusinessDays.java.bak"));    // easy to roll back
    }
}
