package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InMemoryIndexTest {
    private static InMemoryIndex cached;

    /** Built on first use, so every test fails on its own (not the whole class) while addDocument is still a stub. */
    private static InMemoryIndex index() throws IOException {
        if (cached == null) {
            InMemoryIndex built = new InMemoryIndex();
            for (DocumentLoader.Document d : DocumentLoader.load(Path.of("sample_docs"))) built.addDocument(d, 500, 50);
            cached = built;
        }
        return cached;
    }

    @Test void chunksAreNumberedFromOnePerDocument() throws IOException {
        List<InMemoryIndex.ScoredChunk> all = index().search("settlement", 100);
        assertTrue(index().size() >= 5);
        assertTrue(all.stream().anyMatch(s -> s.chunk().docName().equals("settlement_and_cutoff_policy.txt") && s.chunk().chunkNo() == 1));
        assertTrue(all.stream().noneMatch(s -> s.chunk().chunkNo() < 1));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "What time is the cutoff for same-day settlement?|settlement_and_cutoff_policy.txt",
            "Which column holds the settlement status of a trade?|sql_reference_trade_tables.txt",
            "Do client-specific confirmation templates need legal sign-off?|trade_confirmation_rules.txt"})
    void searchFindsTheRightDocumentFirst(String question, String expectedDoc) throws IOException {
        assertEquals(expectedDoc, index().search(question, 1).get(0).chunk().docName());
    }

    @Test void searchReturnsBestFirstAndRespectsTopK() throws IOException {
        List<InMemoryIndex.ScoredChunk> hits = index().search("failed settlement report", 3);
        assertEquals(3, hits.size());
        assertTrue(hits.get(0).score() >= hits.get(1).score() && hits.get(1).score() >= hits.get(2).score());
    }
}
