package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ChunkerTest {
    private static String words(int n) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < n; k++) sb.append("word").append(k).append(' ');
        return sb.toString();
    }

    @Test void chunksStayWithinSizeAndAreNeverBlank() {
        List<String> chunks = Chunker.chunk(words(800), 500, 50);
        assertTrue(chunks.size() > 3);
        for (String c : chunks) {
            assertFalse(c.isBlank());
            assertTrue(c.length() <= 500, "chunk longer than size: " + c.length());
        }
    }

    @Test void noWordIsLost() {
        String text = words(800);
        Set<String> seen = new HashSet<>();
        for (String c : Chunker.chunk(text, 500, 50)) seen.addAll(List.of(c.split("\\s+")));
        assertTrue(seen.containsAll(List.of(text.trim().split("\\s+"))));
    }

    @Test void chunksStartAndEndOnWordBoundaries() {
        String text = words(800);
        for (String c : Chunker.chunk(text, 500, 50)) {
            int pos = text.indexOf(c);
            assertTrue(pos >= 0, "a chunk must be an exact slice of the text");
            assertTrue(pos == 0 || Character.isWhitespace(text.charAt(pos - 1)), "chunk starts mid-word: " + c.substring(0, 12));
            int end = pos + c.length();
            assertTrue(end == text.length() || Character.isWhitespace(text.charAt(end)), "chunk ends mid-word");
        }
    }

    @Test void consecutiveChunksOverlap() {
        List<String> chunks = Chunker.chunk(words(400), 500, 50);
        boolean overlapped = false;
        for (int k = 0; k + 1 < chunks.size(); k++) {
            String[] a = chunks.get(k).split("\\s+");
            Set<String> tail = new HashSet<>(List.of(a).subList(Math.max(0, a.length - 3), a.length));
            for (String w : chunks.get(k + 1).split("\\s+")) {
                if (tail.contains(w)) { overlapped = true; break; }
            }
        }
        assertTrue(overlapped, "expected some words to appear at the end of one chunk and the start of the next");
    }

    @Test void shortTextIsOneChunk() {
        assertEquals(List.of("hello world"), Chunker.chunk("hello world", 500, 50));
    }

    @Test void alwaysFinishesEvenWithoutWhitespace() {
        List<String> chunks = assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> Chunker.chunk("x".repeat(1200), 500, 50), "chunk() never finished: look at the end of the text");
        assertTrue(chunks.stream().mapToInt(String::length).sum() >= 1200);
    }

    @Test void rejectsBadParameters() {
        assertThrows(IllegalArgumentException.class, () -> Chunker.chunk("abc", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Chunker.chunk("abc", 10, 10));
        assertThrows(IllegalArgumentException.class, () -> Chunker.chunk("abc", 10, -1));
    }
}
