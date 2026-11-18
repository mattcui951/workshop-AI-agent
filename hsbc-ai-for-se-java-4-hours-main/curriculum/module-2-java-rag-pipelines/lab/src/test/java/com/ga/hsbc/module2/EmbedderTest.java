package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class EmbedderTest {
    @Test void hasTheRightSizeIsNormalisedAndRepeatable() {
        double[] a = Embedder.embed("same day settlement cutoff");
        double[] b = Embedder.embed("same day settlement cutoff");
        assertEquals(Embedder.DIM, a.length);
        assertArrayEquals(a, b);
        assertEquals(1.0, Embedder.similarity(a, a), 1e-9);
    }

    @Test void stopwordOnlyTextCarriesNoSignal() {
        assertTrue(Arrays.stream(Embedder.embed("what is the")).allMatch(x -> x == 0.0));
    }

    @Test void similarTextScoresHigherThanUnrelatedText() {
        double[] q = Embedder.embed("settlement cutoff time");
        assertTrue(Embedder.similarity(q, Embedder.embed("the settlement cutoff time is 13:00"))
                > Embedder.similarity(q, Embedder.embed("banana bread recipe")));
    }
}
