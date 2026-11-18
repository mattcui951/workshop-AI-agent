package com.ga.hsbc.module4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Keeps file tools inside one repository folder: no absolute paths, no "..", no symlink escapes. */
public final class PathGuard {
    private PathGuard() {}

    public static Path resolveInside(Path root, String relative) throws IOException {
        if (relative == null || relative.isBlank() || relative.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("A relative path is required");
        }
        Path rootReal = root.toRealPath();
        Path p = Path.of(relative);
        if (p.isAbsolute()) throw new IllegalArgumentException("Absolute paths are not allowed");
        Path resolved = rootReal.resolve(p).normalize();
        if (!resolved.startsWith(rootReal)) throw new IllegalArgumentException("Path escapes the repository");
        if (Files.exists(resolved) && !resolved.toRealPath().startsWith(rootReal)) {
            throw new IllegalArgumentException("Path escapes the repository (symlink)");
        }
        return resolved;
    }
}
