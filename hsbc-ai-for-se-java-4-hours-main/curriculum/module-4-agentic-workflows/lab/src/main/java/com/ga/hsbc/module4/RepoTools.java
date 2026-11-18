package com.ga.hsbc.module4;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** File tools for the PR-review agent. The agent can read and propose; it has no tool that writes. */
public final class RepoTools {
    public static final int MAX_FILE_CHARS = 20_000;
    private RepoTools() {}

    /** Lists source files and the error log. */
    public static Tool listFiles(Path root) {
        return new SimpleTool("list_files", "List the Java source files and the error log in the repository.",
                Map.of("type", "object", "properties", Map.of()), input -> {
            Path rootReal = root.toRealPath();
            try (Stream<Path> s = Files.walk(rootReal)) {
                List<String> names = s.filter(Files::isRegularFile)
                        .map(p -> rootReal.relativize(p).toString())
                        .filter(n -> n.endsWith(".java") || n.equals("logs/error.log"))
                        .sorted().limit(200).toList();
                return String.join("\n", names);
            }
        });
    }

    /** Reads one Java source file under src/. */
    public static Tool readFile(Path root) {
        return new SimpleTool("read_file", "Read one Java source file from src/. Input: path relative to the repository root.",
                Map.of("type", "object", "properties", Map.of("path", Map.of("type", "string")), "required", List.of("path")), input -> {
            Path p = PathGuard.resolveInside(root, MiniJson.str(input.get("path")));
            String rel = root.toRealPath().relativize(p).toString();
            if (!rel.startsWith("src") || !rel.endsWith(".java")) throw new IllegalArgumentException("Only Java files under src/ can be read");
            return cap(Files.readString(p, StandardCharsets.UTF_8));
        });
    }

    /** Reads the error log (the last part, if it is long). */
    public static Tool readErrorLog(Path root) {
        return new SimpleTool("read_error_log", "Read the service's error log.",
                Map.of("type", "object", "properties", Map.of()), input -> {
            String log = Files.readString(PathGuard.resolveInside(root, "logs/error.log"), StandardCharsets.UTF_8);
            return log.length() > 8000 ? log.substring(log.length() - 8000) : log;
        });
    }

    /** Records a proposed fix. It does NOT touch the file - a human approves applying it later. */
    public static Tool proposePatch(Path root, List<PendingPatch> sink) {
        return new SimpleTool("propose_patch",
                "Propose a fix by giving the COMPLETE corrected content of one Java file under src/main/java, plus a short explanation. "
                        + "This only records a proposal for human review; nothing is changed.",
                Map.of("type", "object",
                        "properties", Map.of("path", Map.of("type", "string"), "new_content", Map.of("type", "string"),
                                "explanation", Map.of("type", "string")),
                        "required", List.of("path", "new_content", "explanation")), input -> {
            Path p = PathGuard.resolveInside(root, MiniJson.str(input.get("path")));
            String rel = root.toRealPath().relativize(p).toString();
            if (!rel.startsWith("src/main/java/") || !rel.endsWith(".java")) throw new IllegalArgumentException("Patches are only accepted for Java files under src/main/java");
            if (!Files.isRegularFile(p)) throw new IllegalArgumentException("File does not exist: " + rel);
            String content = MiniJson.str(input.get("new_content"));
            if (content == null || content.isBlank() || content.length() > MAX_FILE_CHARS) throw new IllegalArgumentException("new_content is missing or too large");
            sink.add(new PendingPatch(rel, content, MiniJson.str(input.get("explanation"))));
            return "Proposal recorded for human review. It has NOT been applied.";
        });
    }

    private static String cap(String s) { return s.length() > MAX_FILE_CHARS ? s.substring(0, MAX_FILE_CHARS) + "\n... [truncated]" : s; }

    @FunctionalInterface private interface Body { String run(Map<String, Object> input) throws Exception; }

    private record SimpleTool(String name, String description, Map<String, Object> inputSchema, Body body) implements Tool {
        @Override public String execute(Map<String, Object> input) throws Exception { return body.run(input); }
    }
}
