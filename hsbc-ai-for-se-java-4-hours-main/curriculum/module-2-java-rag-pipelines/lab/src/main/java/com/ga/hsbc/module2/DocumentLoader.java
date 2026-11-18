package com.ga.hsbc.module2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/** Loads every .txt file in a folder as a (name, text) pair. */
public final class DocumentLoader {
    public record Document(String name, String text) {}

    private DocumentLoader() {}

    public static List<Document> load(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted()
                    .map(p -> {
                        try {
                            return new Document(p.getFileName().toString(),
                                    Files.readString(p, StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new IllegalStateException("Could not read " + p, e);
                        }
                    })
                    .toList();
        }
    }
}
