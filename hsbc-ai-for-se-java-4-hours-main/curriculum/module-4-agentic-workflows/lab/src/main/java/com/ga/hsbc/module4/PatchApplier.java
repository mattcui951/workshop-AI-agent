package com.ga.hsbc.module4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/** The only code that writes to the repository - and only after a human says yes. Keeps a .bak of the old file. */
public final class PatchApplier {
    private PatchApplier() {}

    public static String diff(Path root, PendingPatch patch) throws IOException {
        Path target = PathGuard.resolveInside(root, patch.path());
        return SimpleDiff.render(patch.path(), Files.readString(target, StandardCharsets.UTF_8), patch.newContent());
    }

    /** @return true if the file was changed */
    public static boolean applyIfApproved(Path root, PendingPatch patch, ApprovalGate gate) throws IOException {
        Path target = PathGuard.resolveInside(root, patch.path());
        boolean approved = gate.approve("apply_patch", Map.of("path", patch.path(), "explanation", String.valueOf(patch.explanation())));
        if (!approved) return false;
        Files.copy(target, target.resolveSibling(target.getFileName() + ".bak"), StandardCopyOption.REPLACE_EXISTING);
        Files.writeString(target, patch.newContent(), StandardCharsets.UTF_8);
        return true;
    }
}
