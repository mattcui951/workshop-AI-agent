// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module2;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The whole "vector store": a list of chunks searched by brute force. No database needed at this size. */
public final class InMemoryIndex {
    public record Chunk(String docName, int chunkNo, String text) {}
    public record ScoredChunk(Chunk chunk, double score) {}

    private record Entry(Chunk chunk, double[] vector) {}

    private final List<Entry> entries = new ArrayList<>();

    public void add(Chunk chunk) {
        entries.add(new Entry(chunk, Embedder.embed(chunk.text())));
    }

    public void addDocument(DocumentLoader.Document doc, int size, int overlap) {
        List<String> pieces = Chunker.chunk(doc.text(), size, overlap);
        for (int k = 0; k < pieces.size(); k++) {
            add(new Chunk(doc.name(), k + 1, pieces.get(k)));
        }
    }

    public int size() { return entries.size(); }

    public List<ScoredChunk> search(String query, int topK) {
        double[] q = Embedder.embed(query);
        return entries.stream()
                .map(e -> new ScoredChunk(e.chunk(), Embedder.similarity(q, e.vector())))
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(topK)
                .toList();
    }
}
