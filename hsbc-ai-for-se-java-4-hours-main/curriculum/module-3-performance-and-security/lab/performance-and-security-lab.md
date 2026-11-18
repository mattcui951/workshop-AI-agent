![General Assembly](/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Refactor and Audit Lab

## Introduction

Code that passes its tests can still be slow or unsafe. In this lab you do two short jobs, each with Copilot and each proven by tests:

- **Part A:** make a slow class fast **without changing what it does**, then measure the speed-up.
- **Part B:** fix two security problems in code you have never seen, and prove each fix with a test.

---

## Learning Objectives

By the end of this lab, you'll be able to:

- Refactor code with an assistant while tests keep its behaviour the same
- Measure a refactor, and tell a better algorithm apart from tidier code
- Fix a security finding with an assistant and prove the fix with a regression test
- Spot a problem that scanners miss, by reading the code

---

## How This Lab Works (read this first)

- **Time:** about 35 minutes for the core task, plus a bonus section.
- **This lab stands on its own.** You do not need anything from Modules 1 or 2.
- **Two small projects:** `refactor/` (Part A) and `audit-target/` (Part B). You run everything from the `lab` folder with the scripts in `scripts/`.
- **`audit-target` is intentionally vulnerable.** It is training code. Never copy it into a real project.
- **Never edit anything in `src/test`.** The tests are the safety net.
- **Stuck for more than 3 minutes?** Every step has a catch-up command.

---

## Part 0: Open the Lab (2 minutes)

1. In VS Code, click **File > Open Folder...**, then go to `repo` > `HSBC-AI-for-SE-Java-4-hours` > `curriculum` > `module-3-performance-and-security` > `lab`, and click **Open**. If asked, click **Yes, I trust the authors**.
2. Open a terminal: **Terminal > New Terminal**.
3. Check Copilot is signed in: the Copilot icon in the bottom Status Bar shows no warning.

| File or folder | What it is |
|---|---|
| `refactor/.../TradeReconciler.java` | The slow class **you refactor** in Part A |
| `refactor/src/test/` | The safety net, including a frozen copy of the old code (`LegacyTradeReconciler`) that your version is compared against |
| `audit-target/src/main/java/com/atlas/inventory/` | Four small classes **you fix** in Part B |
| `scanner-report.md` | What the security scanners found (simulated for the lab) |
| `findings-template.md` | Your findings table |
| `prompts/` | `refactor.md` and `audit-fix.md` |

---

## Part A: Refactor and Measure (15 minutes)

### Step A1: See the starting state (3 minutes)

1. In the terminal, type:

   ```bash
   bash scripts/test-refactor.sh
   ```

2. You should see `NOT YET`. **One** test fails on purpose: `PerformanceTest`. It requires the new code to be at least 3 times faster than the old code. The other 11 tests pass. They check that the code gives the right answers.
3. Open `refactor/src/main/java/com/ga/hsbc/module3/recon/TradeReconciler.java` and find `findDuplicateTradeIds`. It has a loop inside a loop. For 8,000 trades, that is about 64 million comparisons.

*Done when:* you have seen exactly one failing test, `PerformanceTest`.

### Step A2: Refactor with Copilot (8 minutes)

1. In Copilot Chat, choose **Agent** mode.
2. Attach `TradeReconciler.java` and `prompts/refactor.md`. Type `Follow the attached prompt file.`
3. If Copilot asks to edit files, click **Allow**. If it asks to run a command, click **Skip**. Read the change, then click **Keep** if you see it.
4. Run the tests again:

   ```bash
   bash scripts/test-refactor.sh
   ```

5. One of three things happens. All three teach something:
   - **`PASSED`:** faster, and the behaviour is unchanged.
   - **An `OracleEquivalenceTest` or `TradeReconcilerTest` test fails:** Copilot changed the behaviour, and the safety net caught it. The most common cause is the **order** of the duplicate ids. For input `A, B, B, A`, the answer must be `A, B` (the order each id was **first** seen), not `B, A`. Tell Copilot the test name and the failure, and ask it to fix only that.
   - **Only `PerformanceTest` still fails:** the code is tidier but uses the same slow algorithm. Ask Copilot: `findDuplicateTradeIds is still O(n^2). Make it a single pass with a map or set, keeping first-occurrence order.`
6. Read Copilot's list of "behaviours that could differ". Can you find a test for each one?

*Done when:* `bash scripts/test-refactor.sh` says `PASSED`.

*Stuck for more than 3 minutes?* Run `bash scripts/catch-up.sh refactor`, then `bash scripts/test-refactor.sh`.

### Step A3: Measure it (4 minutes)

1. Type:

   ```bash
   bash scripts/benchmark.sh
   ```

2. It prints how long the old and new code take for each method. *Typical result (your numbers will differ):* `findDuplicateTradeIds` drops from about 200 ms to a few milliseconds. `notionalByMetal` and `countByStatus` barely change.
3. Answer in one sentence: **which change made the difference?** Only the algorithm change speeds things up. Tidier code (streams instead of loops) is easier to read, but it is not faster. Never claim the first kind of result for the second kind of change.

*Done when:* you can say which method got faster and why.

---

## Part B: Fix Two Security Findings (18 minutes)

### Step B1: Read before you trust the scanner (4 minutes)

1. Open the four files in `audit-target/src/main/java/com/atlas/inventory/` and read them for **2 minutes**. What worries you?
2. Open `scanner-report.md`. Compare its list with yours. **One real problem is not in the scanner report.** Can you find it? (Look at what `StatementExporter` prints and logs.)
3. Run all the security tests once:

   ```bash
   bash scripts/test-security.sh all
   ```

   You should see `NOT YET`: 6 tests fail and 1 passes. The failures show the real damage. A SQL injection payload returns **every** row, secrets are read from the source code, and the full account number appears in the statement and the log.

*Done when:* you have spotted the problem the scanner missed.

### Step B2: Fix the SQL injection (7 minutes)

1. In Copilot Chat (**Agent** mode), attach `audit-target/src/main/java/com/atlas/inventory/InventoryRepository.java` and `prompts/audit-fix.md`.
2. Type: `Finding: SQL injection in findByMetal (the query is built by joining strings). Follow the attached prompt file.`
3. Read the change. It should use a `PreparedStatement` with a `?` placeholder instead of `+ metalCode +`. Click **Keep** if you see it.
4. Prove it:

   ```bash
   bash scripts/test-security.sh sql
   ```

*Done when:* it says `PASSED` (3 tests: the normal lookup still works, and both injection payloads are harmless).

*Stuck?* `bash scripts/catch-up.sh sql`

### Step B3: Mask the account number (7 minutes)

1. Attach `audit-target/src/main/java/com/atlas/inventory/StatementExporter.java` and `prompts/audit-fix.md`.
2. Type: `Finding: the full account number is written to the statement and to the log. Mask it to the last four characters everywhere. Follow the attached prompt file.`
3. Read the change. **Both** the log line and the statement must be masked. Copilot often fixes only one. Click **Keep** if you see it.
4. Prove it:

   ```bash
   bash scripts/test-security.sh mask
   ```

5. Open `findings-template.md` and fill in rows 1 and 2. Put your name in "Reviewer sign-off". You are the person who read the change.

*Done when:* `bash scripts/test-security.sh mask` says `PASSED` and rows 1 and 2 are filled in.

*Stuck?* `bash scripts/catch-up.sh mask`

---

## You Finished the Core Task

You made slow code fast without changing its behaviour, measured the result, and fixed two security findings with proof. The key idea: **tests prove it is right, a measurement proves it is fast, and a person who read the change proves it is safe.**

---

## Bonus: Keep Going (on your own or if you finish early)

1. **Fix the hard-coded secrets.** Attach `AppConfig.java` and `prompts/audit-fix.md` with: `Finding: hard-coded database password and API key. Read them from the environment function in the constructor, and throw an exception if one is missing.` Prove it with `bash scripts/test-security.sh secrets`. The scanner flagged only the password. Did you notice the API key too?
2. **Remove the vulnerable libraries.** `pom.xml` in `audit-target` includes `log4j-core` 2.14.1 (Log4Shell) and `commons-text` 1.9 (Text4Shell). Prove they are unused: `grep -rn "org.apache.logging\|org.apache.commons.text" audit-target/src/main` (no output means unused). Delete both `<dependency>` blocks, then check the build: `cd audit-target && mvn -q compile && cd ..`. Why is removing them better than upgrading them? (Hint: unused code has risk and no benefit, and an upgrade might need a version this VM does not have.)
3. **All green.** Run `bash scripts/test-security.sh all`. All 7 should pass.
4. **Governance.** Answer the four questions at the bottom of `findings-template.md`. Then write one line each: which fix would you merge on a Friday afternoon, and which would you not?
5. **One more test.** Ask Copilot (Ask mode) what the security tests do **not** check. Write one more test in a new file for that case. (Do not edit the existing test file.)

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `No such file or directory: scripts/...` | The terminal is not in the `lab` folder | `cd ~/repo/HSBC-AI-for-SE-Java-4-hours/curriculum/module-3-performance-and-security/lab` |
| `PerformanceTest` fails even after the refactor | The algorithm is still a loop inside a loop, or the VM is very busy | Check for a single pass; rerun once. If it still fails, ask your instructor |
| `sameDuplicatesInTheSameOrder` fails | The order of the duplicate ids changed | The answer must follow first-occurrence order (A, B, B, A gives A, B) |
| `logsNeverContainTheFullAccountNumber` fails | Only the statement was masked, not the log | Mask the account number in the `LOG.info` line too |
| `COMPILATION ERROR` | A typo in Copilot's change | Ask Copilot to fix only the compile error |
| You want to start this lab again | | `bash scripts/start-over.sh`, then type `yes`. Resets this lab folder only |

---

## Deliverables

- `bash scripts/test-refactor.sh` reports `PASSED`, and you can explain the benchmark
- `bash scripts/test-security.sh sql` and `bash scripts/test-security.sh mask` report `PASSED`
- Rows 1 and 2 of `findings-template.md`, signed by you

## Additional Resources

- OWASP Top 10:2025: <https://owasp.org/Top10/2025/>
- OWASP SQL Injection Prevention Cheat Sheet: <https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html>
- OWASP Dependency-Check: <https://owasp.org/www-project-dependency-check/>
