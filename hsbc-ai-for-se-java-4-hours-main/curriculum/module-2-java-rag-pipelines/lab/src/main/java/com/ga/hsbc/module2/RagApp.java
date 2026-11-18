package com.ga.hsbc.module2;

import java.nio.file.Path;

/** Shared wiring so the CLI demo, the tests and the Vert.x endpoint all build the index the same way. */
public final class RagApp {
    public static final double MIN_RELEVANCE = 0.20;   // tuned for THIS corpus - re-tune whenever the documents change
    public static final int TOP_K = 3;

    private RagApp() {}

    public static InMemoryIndex buildIndex(Path docsFolder) throws java.io.IOException {
        InMemoryIndex index = new InMemoryIndex();
        for (DocumentLoader.Document d : DocumentLoader.load(docsFolder)) {
            index.addDocument(d, 500, 50);
        }
        return index;
    }

    public static RagService service(InMemoryIndex index, LlmClient llm) {
        return new RagService(index, llm, MIN_RELEVANCE, TOP_K);
    }
}
