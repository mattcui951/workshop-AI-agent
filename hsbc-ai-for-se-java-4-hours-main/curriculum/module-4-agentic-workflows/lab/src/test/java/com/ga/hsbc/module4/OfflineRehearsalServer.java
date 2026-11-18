package com.ga.hsbc.module4;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * OFFLINE REHEARSAL: a scripted stand-in for the Claude API so an instructor can dry-run the Module 4 demos
 * with no key and no network. It is NOT a model - it replays a fixed script - so use it to rehearse the flow
 * and the approval gates, never to judge what Claude would actually do.
 *
  *   (the pr-review script reads the reference fix from ../solutions/BusinessDays.fixed.java, so run it from the lab folder)
 *   java -cp <test-classes> com.ga.hsbc.module4.OfflineRehearsalServer agent|pr-review [port]
 * then, in another terminal:
 *   ANTHROPIC_API_KEY=offline ANTHROPIC_BASE_URL=http://127.0.0.1:8089 java ... AgentApp   (or PrReviewApp)
 */
public final class OfflineRehearsalServer {
    public static void main(String[] args) throws Exception {
        String scenario = args.length > 0 ? args[0] : "agent";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8089;

        List<String> script = switch (scenario) {
            case "agent" -> List.of(
                    ScriptedAnthropic.toolUse("toolu_1", "query_trade_db", Map.of("sql",
                            "SELECT SUM(quantity_oz) AS total_oz FROM trade WHERE metal_code = 'AU' AND status = 'SETTLED'")),
                    ScriptedAnthropic.toolUse("toolu_2", "get_metal_price", Map.of("metal_code", "AU")),
                    ScriptedAnthropic.finalText("750 troy ounces of gold have settled (from the trade table). "
                            + "At today's gold price of 2,321.50 USD per ounce (from the price API) that is about 1,741,125 USD."));
            case "pr-review" -> {
                String fixed = Files.readString(Path.of("..", "solutions", "BusinessDays.fixed.java"), StandardCharsets.UTF_8);
                yield List.of(
                        ScriptedAnthropic.toolUse("t1", "list_files", Map.of()),
                        ScriptedAnthropic.toolUse("t2", "read_error_log", Map.of()),
                        ScriptedAnthropic.toolUse("t3", "read_file", Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java")),
                        ScriptedAnthropic.toolUse("t4", "propose_patch", Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java",
                                "new_content", fixed,
                                "explanation", "previousBusinessDay only skips Sunday, so a Monday run resolves to Saturday, finds no trades and divides by zero.")),
                        ScriptedAnthropic.finalText("Root cause: BusinessDays.previousBusinessDay returns Saturday for a Monday run, "
                                + "so the job finds 0 trades and ReconciliationJob divides by zero. I proposed a fix in BusinessDays. "
                                + "I did not change ReconciliationJob; you may also want a guard for an empty trade list."));
            }
            default -> throw new IllegalArgumentException("scenario must be agent or pr-review");
        };

        ScriptedAnthropic server = new ScriptedAnthropic(script, port);
        System.out.println("Offline rehearsal server (" + scenario + ") listening on " + server.baseUrl() + "  - scripted replies, not a real model");
        Thread.currentThread().join();
    }
}
