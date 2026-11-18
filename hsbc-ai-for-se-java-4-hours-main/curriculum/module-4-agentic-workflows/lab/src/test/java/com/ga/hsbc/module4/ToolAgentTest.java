package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ToolAgentTest {
    private static final String SYSTEM = "test system prompt";

    private ToolRegistry registry(MetalPriceService prices) throws Exception {
        return new ToolRegistry()
                .register(new QueryTradesTool(Database.agentConnection()))
                .register(new MetalPriceTool(prices.baseUrl()));
    }

    @Test void runsApprovedToolsAndFeedsResultsBackToTheModel() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        List<String> approvals = new ArrayList<>();
        ApprovalGate gate = (action, details) -> { approvals.add(action); return true; };
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "query_trade_db", Map.of("sql",
                        "SELECT SUM(quantity_oz) AS total_oz FROM trade WHERE metal_code = 'AU' AND status = 'SETTLED'")),
                ScriptedAnthropic.toolUse("toolu_2", "get_metal_price", Map.of("metal_code", "AU")),
                ScriptedAnthropic.finalText("750 oz settled, worth about 1,741,125 USD.")))) {

            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), gate, 6).run(SYSTEM, "How much gold settled?");

            assertEquals("end_turn", r.stopReason());
            assertEquals(3, r.steps());
            assertEquals("750 oz settled, worth about 1,741,125 USD.", r.finalText());
            assertEquals(List.of("query_trade_db", "get_metal_price"), approvals);       // a human saw every call
            assertEquals(2, r.audit().size());
            assertTrue(r.audit().stream().allMatch(c -> c.approved() && !c.error()));

            // The 2nd request must carry the 1st tool's real result back to the model, linked by tool_use_id.
            Map<String, Object> second = api.requests.get(1);
            List<Object> msgs = MiniJson.arr(second.get("messages"));
            assertEquals(3, msgs.size());                                                // user, assistant(tool_use), user(tool_result)
            Map<String, Object> toolResult = MiniJson.obj(MiniJson.arr(MiniJson.obj(msgs.get(2)).get("content")).get(0));
            assertEquals("tool_result", toolResult.get("type"));
            assertEquals("toolu_1", toolResult.get("tool_use_id"));
            assertTrue(MiniJson.str(toolResult.get("content")).contains("750"));
            // and every request advertised both tools with schemas
            assertEquals(2, MiniJson.arr(second.get("tools")).size());
        } finally { prices.stop(); }
    }

    @Test void aDeclinedCallNeverRunsAndTheModelIsToldWhy() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "get_metal_price", Map.of("metal_code", "AU")),
                ScriptedAnthropic.finalText("I could not get the price because the call was declined.")))) {

            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), (a, d) -> false, 6).run(SYSTEM, "price?");

            assertFalse(r.audit().get(0).approved());
            assertEquals("declined by human reviewer", r.audit().get(0).resultPreview());
            Map<String, Object> toolResult = lastToolResult(api.requests.get(1));
            assertEquals(Boolean.TRUE, toolResult.get("is_error"));
            assertTrue(MiniJson.str(toolResult.get("content")).contains("declined"));
        } finally { prices.stop(); }
    }

    @Test void evenIfAHumanApprovesAWriteTheSqlToolStillBlocksIt() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "query_trade_db", Map.of("sql", "DELETE FROM trade")),
                ScriptedAnthropic.finalText("The delete was blocked.")))) {

            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), (a, d) -> true, 6).run(SYSTEM, "clean up");

            assertTrue(r.audit().get(0).approved());
            assertTrue(r.audit().get(0).error());
            assertEquals(Boolean.TRUE, lastToolResult(api.requests.get(1)).get("is_error"));
            try (Connection c = Database.adminConnection(); Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM trade")) {
                rs.next();
                assertEquals(8, rs.getInt(1));                                           // data intact
            }
        } finally { prices.stop(); }
    }

    @Test void unknownToolsAreRefused() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "send_email", Map.of("to", "x@example.com")),
                ScriptedAnthropic.finalText("done")))) {
            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), (a, d) -> true, 6).run(SYSTEM, "email");
            assertEquals("unknown tool", r.audit().get(0).resultPreview());
            assertEquals(Boolean.TRUE, lastToolResult(api.requests.get(1)).get("is_error"));
        } finally { prices.stop(); }
    }

    @Test void theLoopStopsAtTheStepLimit() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(    // the last scripted reply repeats forever
                ScriptedAnthropic.toolUse("toolu_x", "get_metal_price", Map.of("metal_code", "AG"))))) {
            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), (a, d) -> true, 3).run(SYSTEM, "loop");
            assertEquals("max_steps", r.stopReason());
            assertEquals(3, api.requests.size());                                         // exactly 3 model calls, then it stops
            assertTrue(r.finalText().contains("limit"));
        } finally { prices.stop(); }
    }

    @Test void aToolThatCrashesIsReportedToTheModelNotThrown() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "get_metal_price", Map.of("metal_code", "XX")),
                ScriptedAnthropic.finalText("unsupported")))) {
            ToolAgent.AgentResult r = new ToolAgent(api.client(), registry(prices), (a, d) -> true, 6).run(SYSTEM, "xx?");
            assertTrue(r.audit().get(0).approved());
            assertTrue(r.audit().get(0).error());
            assertEquals(Boolean.TRUE, lastToolResult(api.requests.get(1)).get("is_error"));
        } finally { prices.stop(); }
    }

    @Test void longToolOutputIsTruncatedBeforeItGoesBack() throws Exception {
        MetalPriceService prices = new MetalPriceService(0);
        Tool firehose = new Tool() {
            public String name() { return "firehose"; }
            public String description() { return "lots of text"; }
            public Map<String, Object> inputSchema() { return Map.of("type", "object", "properties", Map.of()); }
            public String execute(Map<String, Object> input) { return "x".repeat(10_000); }
        };
        try (ScriptedAnthropic api = new ScriptedAnthropic(List.of(
                ScriptedAnthropic.toolUse("toolu_1", "firehose", Map.of()), ScriptedAnthropic.finalText("ok")))) {
            new ToolAgent(api.client(), registry(prices).register(firehose), (a, d) -> true, 6).run(SYSTEM, "go");
            String content = MiniJson.str(lastToolResult(api.requests.get(1)).get("content"));
            assertTrue(content.length() < 4100 && content.endsWith("[truncated]"));
        } finally { prices.stop(); }
    }

    private static Map<String, Object> lastToolResult(Map<String, Object> request) {
        List<Object> msgs = MiniJson.arr(request.get("messages"));
        Map<String, Object> last = MiniJson.obj(msgs.get(msgs.size() - 1));
        return MiniJson.obj(MiniJson.arr(last.get("content")).get(0));
    }
}
