package com.ga.hsbc.module4;

import java.util.ArrayList;
import java.util.List;

/** Minimal unified-style diff for showing a reviewer what a patch changes (one changed block, 2 lines of context). */
public final class SimpleDiff {
    private SimpleDiff() {}

    public static String render(String path, String oldText, String newText) {
        List<String> a = oldText.lines().toList();
        List<String> b = newText.lines().toList();
        int prefix = 0;
        while (prefix < a.size() && prefix < b.size() && a.get(prefix).equals(b.get(prefix))) prefix++;
        int suffix = 0;
        while (suffix < a.size() - prefix && suffix < b.size() - prefix
                && a.get(a.size() - 1 - suffix).equals(b.get(b.size() - 1 - suffix))) suffix++;

        List<String> out = new ArrayList<>();
        out.add("--- a/" + path);
        out.add("+++ b/" + path);
        if (prefix == a.size() && prefix == b.size()) { out.add("(no changes)"); return String.join("\n", out); }
        for (int k = Math.max(0, prefix - 2); k < prefix; k++) out.add("  " + a.get(k));
        for (int k = prefix; k < a.size() - suffix; k++) out.add("- " + a.get(k));
        for (int k = prefix; k < b.size() - suffix; k++) out.add("+ " + b.get(k));
        for (int k = a.size() - suffix; k < Math.min(a.size(), a.size() - suffix + 2); k++) out.add("  " + a.get(k));
        return String.join("\n", out);
    }
}
