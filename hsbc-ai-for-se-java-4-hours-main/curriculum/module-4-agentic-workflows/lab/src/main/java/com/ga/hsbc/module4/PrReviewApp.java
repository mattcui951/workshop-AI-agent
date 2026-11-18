package com.ga.hsbc.module4;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * YOUR JOB (You Do): the human approval gate is broken. Read makeGate(), fix it, then make
 *     mvn -q test -Dtest=PrReviewGateTest  go green.
 *
 * PR-review / triage agent: java -cp ... com.ga.hsbc.module4.PrReviewApp [path-to-repo]   (needs ANTHROPIC_API_KEY)
 * The agent reads the log and code and PROPOSES a fix. A human approves before anything is written.
 */
public final class PrReviewApp {
    static final String SYSTEM = """
            You are a code review and triage assistant for a Java service. You can list files, read source files and
            read the error log. You CANNOT change anything. When you have found the root cause, call propose_patch
            once with the complete corrected file and a short explanation. Prefer fixing the root cause over hiding
            the symptom, and say what you are unsure about.
            """;

    // Tier approvals by risk: reading and proposing change nothing, so they run freely. Applying a patch is where the human decides.
    static final Set<String> SAFE_TOOLS = Set.of("list_files", "read_file", "read_error_log", "propose_patch");

    /** The gate the agent and the patch applier share: reads and proposals run freely, everything else is decided by the human. */
    static ApprovalGate makeGate(ApprovalGate human) {
        // TODO (lab): this approves EVERY action without asking anyone, so the human is never consulted: not even before a patch is
        //             written to disk. That is not a human gate. Fix it so that:
        //               - the SAFE_TOOLS run freely (reading and proposing change nothing)
        //               - every other action, including "apply_patch", is decided by human.approve(action, details)
        return (action, details) -> true;
    }

    public static void main(String[] args) throws Exception {
        Path repo = Path.of(args.length > 0 ? args[0] : "sample-service");
        ApprovalGate human = ApprovalGate.fromEnvironment();
        ApprovalGate gate = makeGate(human);

        List<PendingPatch> proposals = new ArrayList<>();
        ToolRegistry registry = new ToolRegistry()
                .register(RepoTools.listFiles(repo))
                .register(RepoTools.readFile(repo))
                .register(RepoTools.readErrorLog(repo))
                .register(RepoTools.proposePatch(repo, proposals));

        ToolAgent agent = new ToolAgent(ClaudeToolClient.fromEnv(), registry, gate, 10);
        ToolAgent.AgentResult r = agent.run(SYSTEM,
                "The overnight reconciliation job failed on Monday morning. Find the root cause in this repository and propose a fix.");

        System.out.println("--- Agent's findings ---\n" + r.finalText());
        if (proposals.isEmpty()) { System.out.println("\nNo patch was proposed."); return; }
        PendingPatch patch = proposals.get(proposals.size() - 1);
        System.out.println("\n--- Proposed change (NOT applied yet) ---\n" + PatchApplier.diff(repo, patch));
        boolean applied = PatchApplier.applyIfApproved(repo, patch, gate);
        System.out.println(applied ? "\nApplied. Original saved as a .bak file. Now run the tests yourself."
                                   : "\nNot applied. The repository is unchanged.");
    }
}
