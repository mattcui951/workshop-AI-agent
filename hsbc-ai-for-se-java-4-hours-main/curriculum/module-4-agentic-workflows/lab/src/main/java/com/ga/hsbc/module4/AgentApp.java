package com.ga.hsbc.module4;

/** Interactive demo: java -cp ... com.ga.hsbc.module4.AgentApp ["question"]   (needs ANTHROPIC_API_KEY) */
public final class AgentApp {
    static final String SYSTEM = """
            You are an operations assistant for a precious-metals desk. Use the tools to answer with real data;
            never guess numbers. Quantities are in troy ounces. Say clearly which figures came from which tool.
            If a tool call is declined or fails, explain what you could not do instead of making something up.
            """;

    public static void main(String[] args) throws Exception {
        String question = args.length > 0 ? String.join(" ", args)
                : "How many troy ounces of gold have settled, and what is that worth at today's gold price?";

        MetalPriceService prices = new MetalPriceService(0);          // synthetic internal API on a free local port
        try {
            ToolRegistry registry = new ToolRegistry()
                    .register(new QueryTradesTool(Database.agentConnection()))
                    .register(new MetalPriceTool(prices.baseUrl()));
            ToolAgent agent = new ToolAgent(ClaudeToolClient.fromEnv(), registry, ApprovalGate.fromEnvironment(), 6);

            System.out.println("Question: " + question);
            ToolAgent.AgentResult r = agent.run(SYSTEM, question);

            System.out.println("\n--- Audit trail ---");
            for (ToolAgent.ToolCallRecord c : r.audit()) {
                System.out.printf("%-16s approved=%-5s error=%-5s %s%n", c.tool(), c.approved(), c.error(), c.resultPreview());
            }
            System.out.println("\n--- Answer (" + r.stopReason() + ", " + r.steps() + " model calls) ---");
            System.out.println(r.finalText());
        } finally {
            prices.stop();
        }
    }
}
