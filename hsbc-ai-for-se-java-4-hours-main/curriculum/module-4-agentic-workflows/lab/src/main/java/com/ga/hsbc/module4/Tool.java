package com.ga.hsbc.module4;

import java.util.Map;

/** One capability the model may ask for. The model only ever sees the name, description and input schema. */
public interface Tool {
    String name();
    String description();
    /** JSON Schema describing the input object. */
    Map<String, Object> inputSchema();
    /** Runs the tool. Throw an exception to report a failure back to the model as an error result. */
    String execute(Map<String, Object> input) throws Exception;
}
