package com.ga.hsbc.module4;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Given tests: the PR-review tools. They pass from the start: the agent can read and PROPOSE, and only the applier writes. */
class PrReviewToolsTest {
    @TempDir Path tmp;
    Path repo;

    @BeforeEach void copyRepo() throws IOException {
        repo = tmp.resolve("repo");
        Path src = Path.of("sample-service");
        try (Stream<Path> s = Files.walk(src)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                Path dest = repo.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(dest); else Files.copy(p, dest);
            }
        }
    }

    private String read(String rel) throws IOException { return Files.readString(repo.resolve(rel), StandardCharsets.UTF_8); }

    // ---------- PathGuard ----------
    @Test void pathGuardAllowsPathsInsideTheRepo() throws IOException {
        Path p = PathGuard.resolveInside(repo, "src/main/java/com/atlas/recon/BusinessDays.java");
        assertTrue(p.startsWith(repo.toRealPath()));
    }

    @Test void pathGuardBlocksEscapes() throws IOException {
        for (String bad : List.of("../secrets.txt", "src/../../outside.txt", "/etc/passwd", "", "a\0b")) {
            assertThrows(IllegalArgumentException.class, () -> PathGuard.resolveInside(repo, bad), bad);
        }
        Path outside = Files.writeString(tmp.resolve("outside.txt"), "secret");
        Path link = repo.resolve("src/link.txt");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | IOException e) {
            return;   // filesystem without symlinks: nothing more to check
        }
        assertThrows(IllegalArgumentException.class, () -> PathGuard.resolveInside(repo, "src/link.txt"));
    }

    // ---------- SimpleDiff ----------
    @Test void diffShowsOnlyTheChangedLines() {
        String d = SimpleDiff.render("A.java", "one\ntwo\nthree\nfour\n", "one\nTWO\nthree\nfour\n");
        assertTrue(d.contains("- two"));
        assertTrue(d.contains("+ TWO"));
        assertFalse(d.contains("- one"));
        assertTrue(SimpleDiff.render("A.java", "same\n", "same\n").contains("(no changes)"));
    }

    // ---------- tools ----------
    @Test void listAndReadToolsStayInsideTheirLane() throws Exception {
        String listing = RepoTools.listFiles(repo).execute(Map.of());
        assertTrue(listing.contains("src/main/java/com/atlas/recon/BusinessDays.java"));
        assertTrue(listing.contains("logs/error.log"));

        Tool read = RepoTools.readFile(repo);
        assertTrue(read.execute(Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java")).contains("DayOfWeek.SUNDAY"));
        assertThrows(IllegalArgumentException.class, () -> read.execute(Map.of("path", "logs/error.log")));      // not source
        assertThrows(IllegalArgumentException.class, () -> read.execute(Map.of("path", "../../etc/passwd")));
    }

    @Test void proposePatchRecordsButNeverWrites() throws Exception {
        String before = read("src/main/java/com/atlas/recon/BusinessDays.java");
        List<PendingPatch> sink = new ArrayList<>();
        Tool propose = RepoTools.proposePatch(repo, sink);

        String msg = propose.execute(Map.of("path", "src/main/java/com/atlas/recon/BusinessDays.java",
                "new_content", "package x; class Y {}", "explanation", "e"));

        assertTrue(msg.contains("NOT been applied"));
        assertEquals(1, sink.size());
        assertEquals(before, read("src/main/java/com/atlas/recon/BusinessDays.java"));   // file untouched
        assertThrows(IllegalArgumentException.class, () -> propose.execute(Map.of("path", "src/test/java/com/atlas/recon/BusinessDaysTest.java",
                "new_content", "x", "explanation", "e")));                                // tests are out of bounds
        assertThrows(IllegalArgumentException.class, () -> propose.execute(Map.of("path", "pom.xml", "new_content", "x", "explanation", "e")));
    }

    // ---------- the applier ----------
    @Test void aPatchIsAppliedOnlyAfterApprovalAndABackupIsKept() throws Exception {
        String rel = "src/main/java/com/atlas/recon/BusinessDays.java";
        String original = read(rel);
        PendingPatch patch = new PendingPatch(rel, "// fixed\n", "why");

        assertFalse(PatchApplier.applyIfApproved(repo, patch, (a, d) -> false));          // declined
        assertEquals(original, read(rel));
        assertFalse(Files.exists(repo.resolve(rel + ".bak")));

        assertTrue(PatchApplier.applyIfApproved(repo, patch, (a, d) -> true));            // approved
        assertEquals("// fixed\n", read(rel));
        assertEquals(original, read(rel + ".bak"));                                       // easy to roll back
    }
}
