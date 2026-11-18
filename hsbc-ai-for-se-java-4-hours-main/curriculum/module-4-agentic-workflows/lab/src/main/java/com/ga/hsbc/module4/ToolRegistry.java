package com.ga.hsbc.module4;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The allow-list: the agent can use these tools and nothing else. */
public final class ToolRegistry {
    private final Map<String, Tool> tools = new LinkedHashMap<>();

    public ToolRegistry register(Tool t) {
        tools.put(t.name(), t);
        return this;
    }

    public Optional<Tool> find(String name) { return Optional.ofNullable(tools.get(name)); }

    /** The "tools" array sent to the Messages API. */
    public List<Map<String, Object>> definitions() {
        List<Map<String, Object>> defs = new ArrayList<>();
        for (Tool t : tools.values()) {
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("name", t.name());
            d.put("description", t.description());
            d.put("input_schema", t.inputSchema());
            defs.add(d);
        }
        return defs;
    }
}
