// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module2;

import java.util.Locale;
import java.util.Set;

/**
 * A deliberately simple "embedding": hash each meaningful word into one of
 * DIM buckets and normalise the vector to length 1.
 *
 * This is NOT a real embedding model. It lets the lab run offline, for free,
 * identically on every learner VM, and it is good enough to show retrieval
 * mechanics. In production you would call an embedding model instead.
 */
public final class Embedder {
    public static final int DIM = 1024;

    private static final Set<String> STOPWORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "can", "do", "does", "for", "from",
            "has", "have", "how", "i", "if", "in", "is", "it", "of", "on", "or", "our", "the",
            "to", "was", "we", "what", "when", "which", "who", "will", "with", "you", "your",
            "my", "me", "that", "this", "there", "any", "all", "must", "should", "than", "then", "so");

    private Embedder() {}

    public static double[] embed(String text) {
        double[] v = new double[DIM];
        for (String token : text.toLowerCase(Locale.ROOT).split("[^a-z0-9_]+")) {
            if (token.length() < 2 || STOPWORDS.contains(token)) continue;
            v[Math.floorMod(token.hashCode(), DIM)] += 1.0;
        }
        double norm = 0;
        for (double x : v) norm += x * x;
        norm = Math.sqrt(norm);
        if (norm > 0) for (int k = 0; k < v.length; k++) v[k] /= norm;
        return v;
    }

    /** Cosine similarity; both vectors are already normalised, so this is a dot product. */
    public static double similarity(double[] a, double[] b) {
        double dot = 0;
        for (int k = 0; k < a.length; k++) dot += a[k] * b[k];
        return dot;
    }
}
