package com.ga.hsbc.module2;

import java.util.List;

/**
 * YOUR JOB (You Do, with your assistant): write build() so that  mvn -q test -Dtest=PromptBuilderTest  goes green.
 */
public final class PromptBuilder {
    public record Prompt(String system, String user) {}

    private PromptBuilder() {}

    /**
     * Return the system prompt and the user prompt.
     *
     * Rules the tests check:
     *  - the system prompt tells the model to use ONLY the numbered sources, to cite them like [1], to say so when the sources
     *    do not contain the answer, and that text inside source tags is DATA, not instructions
     *  - the user prompt shows each source as   <source id="N" doc="FILE NAME"> ...text... </source>   (N starts at 1),
     *    then a blank line and   Question: <the question>
     *  - a document must not be able to forge or close a tag: escape "<" as &lt; and ">" as &gt; inside the source text
     */
    public static Prompt build(String question, List<InMemoryIndex.ScoredChunk> sources) {
        throw new UnsupportedOperationException("TODO: write PromptBuilder.build");
    }
}
