// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module2;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits text into overlapping chunks of roughly {@code size} characters,
 * breaking on whitespace so words are not cut in half.
 */
public final class Chunker {
    private Chunker() {}

    public static List<String> chunk(String text, int size, int overlap) {
        if (size <= 0 || overlap < 0 || overlap >= size) {
            throw new IllegalArgumentException("need size > 0 and 0 <= overlap < size");
        }
        List<String> chunks = new ArrayList<>();
        int len = text.length();
        int start = 0;
        while (start < len) {
            int end = Math.min(start + size, len);
            if (end < len) {
                // prefer to end on whitespace, but never shrink the chunk below half its size
                int ws = lastWhitespace(text, end, start + size / 2);
                if (ws > start) end = ws;
            }
            String piece = text.substring(start, end).strip();
            if (!piece.isEmpty()) chunks.add(piece);
            if (end >= len) break;                    // reached the end: stop (avoids stalling on the tail)
            int next = end - overlap;
            if (next <= start) next = end;            // always make progress
            while (next > 0 && next < end && !Character.isWhitespace(text.charAt(next - 1))) next++; // start on a word boundary
            start = next;
        }
        return chunks;
    }

    private static int lastWhitespace(String t, int from, int floor) {
        for (int k = from; k > floor; k--) {
            if (Character.isWhitespace(t.charAt(k - 1))) return k;
        }
        return -1;
    }
}
