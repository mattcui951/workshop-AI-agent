package com.ga.hsbc.module2;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Command-line demo with a human review step:
 *     mvn -q compile exec:java -Dexec.mainClass=com.ga.hsbc.module2.RagDemo -Dexec.args="your question"
 * Retrieval always runs. The answer step needs ANTHROPIC_API_KEY. A grounded answer is a DRAFT until you approve it.
 */
public final class RagDemo {
    public static void main(String[] args) throws Exception {
        String question = args.length > 0 ? String.join(" ", args) : "What time is the cutoff for same-day settlement?";

        InMemoryIndex index = RagApp.buildIndex(Path.of("sample_docs"));
        System.out.println("Indexed " + index.size() + " chunks");
        System.out.println("Question: " + question);
        System.out.println("--- Top retrieved chunks ---");
        var hits = index.search(question, RagApp.TOP_K);
        for (InMemoryIndex.ScoredChunk sc : hits) {
            String preview = sc.chunk().text().replace('\n', ' ');
            System.out.printf("(score=%.3f) %s#%d  %s...%n", sc.score(), sc.chunk().docName(), sc.chunk().chunkNo(),
                    preview.substring(0, Math.min(90, preview.length())));
        }
        if (hits.isEmpty() || hits.get(0).score() < RagApp.MIN_RELEVANCE) {
            System.out.println("\n[guardrail] best score is below " + RagApp.MIN_RELEVANCE + ": the model will not be called.");
        }

        String key = System.getenv("ANTHROPIC_API_KEY");
        if (key == null || key.isBlank()) {
            System.out.println("\n(Set ANTHROPIC_API_KEY to run the answer step.)");
            return;
        }
        RagService.RagAnswer answer = RagApp.service(index, ClaudeClient.fromEnv()).ask(question);
        System.out.println("\n--- Draft answer (grounded=" + answer.grounded() + ") ---");
        System.out.println(answer.answer());
        System.out.println("[metrics] input_tokens=" + answer.inputTokens() + " output_tokens=" + answer.outputTokens());
        if (!answer.grounded()) return;

        ReviewQueue queue = new ReviewQueue();
        String id = queue.submit(answer);
        System.out.println("\nThis is a DRAFT (" + id + "). It is not released until a reviewer approves it.");
        Scanner in = new Scanner(System.in);
        System.out.print("Reviewer name: ");
        String reviewer = in.hasNextLine() ? in.nextLine().trim() : "";
        System.out.print("Approve this answer? (y/n): ");
        String decision = in.hasNextLine() ? in.nextLine().trim().toLowerCase() : "n";
        if (decision.equals("y")) {
            queue.approve(id, reviewer);
            System.out.println("\nReleased to the requester:\n" + queue.releasedAnswer(id).orElseThrow());
        } else {
            queue.reject(id, reviewer, "rejected by reviewer");
            System.out.println("\nRejected. Nothing was released.");
        }
    }
}
