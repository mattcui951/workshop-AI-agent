// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The agent loop:
 *   send question + tool list -> if the model asks for a tool: ask a human, run it, send the result back -> repeat.
 *
 * Safety controls, all visible in this one class:
 *   1. allow-list   - only tools in the registry can run
 *   2. approval     - a human approves every single call
 *   3. step limit   - the loop cannot run forever
 *   4. errors stay  - a failed or declined call is reported to the model as an error result, never hidden
 *   5. output cap   - tool output is truncated before it goes back to the model
 *   6. audit trail  - every call and decision is recorded
 */
public final class ToolAgent {
    public static final int MAX_TOOL_OUTPUT_CHARS = 4000;

    public record ToolCallRecord(String tool, Map<String, Object> input, boolean approved, boolean error, String resultPreview) {}

    public record AgentResult(String finalText, String stopReason, int steps, List<ToolCallRecord> audit) {}

    private final ClaudeToolClient client;
    private final ToolRegistry tools;
    private final ApprovalGate gate;
    private final int maxSteps;

    public ToolAgent(ClaudeToolClient client, ToolRegistry tools, ApprovalGate gate, int maxSteps) {
        this.client = client; this.tools = tools; this.gate = gate; this.maxSteps = maxSteps;
    }

    public AgentResult run(String system, String userQuestion) throws IOException {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userQuestion));
        List<ToolCallRecord> audit = new ArrayList<>();

        for (int step = 1; step <= maxSteps; step++) {
            ClaudeToolClient.ToolResponse resp = client.send(system, messages, tools.definitions());
            messages.add(Map.of("role", "assistant", "content", resp.content()));

            if (!"tool_use".equals(resp.stopReason())) {
                return new AgentResult(resp.text(), resp.stopReason(), step, audit);
            }

            List<Map<String, Object>> results = new ArrayList<>();
            for (Map<String, Object> block : resp.content()) {
                if (!"tool_use".equals(block.get("type"))) continue;
                String id = MiniJson.str(block.get("id"));
                String name = MiniJson.str(block.get("name"));
                Map<String, Object> input = block.get("input") == null ? Map.of() : MiniJson.obj(block.get("input"));
                results.add(handleToolCall(id, name, input, audit));
            }
            messages.add(Map.of("role", "user", "content", results));
        }
        return new AgentResult("(stopped: reached the limit of " + maxSteps + " steps without a final answer)", "max_steps", maxSteps, audit);
    }

    private Map<String, Object> handleToolCall(String id, String name, Map<String, Object> input, List<ToolCallRecord> audit) {
        Optional<Tool> tool = tools.find(name);
        if (tool.isEmpty()) {
            audit.add(new ToolCallRecord(name, input, false, true, "unknown tool"));
            return result(id, "Error: unknown tool '" + name + "'.", true);
        }
        if (!gate.approve(name, input)) {
            audit.add(new ToolCallRecord(name, input, false, true, "declined by human reviewer"));
            return result(id, "Error: the human reviewer declined this tool call.", true);
        }
        try {
            String out = tool.get().execute(input);
            if (out.length() > MAX_TOOL_OUTPUT_CHARS) out = out.substring(0, MAX_TOOL_OUTPUT_CHARS) + "... [truncated]";
            audit.add(new ToolCallRecord(name, input, true, false, preview(out)));
            return result(id, out, false);
        } catch (Exception e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            audit.add(new ToolCallRecord(name, input, true, true, preview(msg)));
            return result(id, "Error: " + msg, true);
        }
    }

    private static Map<String, Object> result(String toolUseId, String content, boolean isError) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "tool_result");
        r.put("tool_use_id", toolUseId);
        r.put("content", content);
        if (isError) r.put("is_error", true);
        return r;
    }

    private static String preview(String s) { return s.length() > 120 ? s.substring(0, 120) + "..." : s; }
}
