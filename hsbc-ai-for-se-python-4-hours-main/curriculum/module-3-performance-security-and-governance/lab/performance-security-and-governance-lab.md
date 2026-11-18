![General Assembly](/curriculum/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Refactor and Audit Lab

## Introduction

Code that passes its tests can still be slow or unsafe. In this lab you do two short jobs, each with Copilot and each proven by tests:

- **Part A:** make a slow function fast **without changing what it does**, then measure the speed-up.
- **Part B:** run a security scanner, then fix two security problems in code you have never seen, and prove each fix with a test.

---

## Learning Objectives

By the end of this lab, you'll be able to:

- Refactor code with an assistant while tests keep its behaviour the same
- Measure a refactor, and tell a better algorithm apart from tidier code
- Fix a security finding with an assistant and prove the fix with a regression test
- Spot a problem that a scanner misses, by reading the code

---

## How This Lab Works (read this first)

- **Time:** about 35 minutes for the core task, plus a bonus section.
- **This lab stands on its own.** You do not need anything from Modules 1 or 2.
- **`audit_target/` is intentionally vulnerable.** It is training code. Never copy it into a real project.
- **Never edit anything in `tests/`.** The tests are the safety net.
- **Stuck for more than 3 minutes?** Every step has a catch-up command.

---

## Part 0: Open the Lab (2 minutes)

1. In VS Code, click **File > Open Folder...**, then go to `repo` > `HSBC-AI-for-SE-Python-4-hours` > `curriculum` > `module-3-performance-security-and-governance` > `lab`, and click **Open**. If asked, click **Yes, I trust the authors**, and choose the `ga-python-venv` interpreter.
2. Open a terminal: **Terminal > New Terminal**.
3. Check Copilot is signed in: the Copilot icon in the bottom Status Bar shows no warning.

| File or folder | What it is |
|---|---|
| `reconciler.py` | The slow code **you refactor** in Part A |
| `transactions.py` | The `Transaction` type (amounts are `Decimal`) |
| `tests/test_reconciler.py`, `tests/test_oracle.py`, `tests/test_performance.py` | The safety net. `tests/legacy_reconciler.py` is a frozen copy of the old code that yours is compared against |
| `benchmark_demo.py` | Times old against new |
| `audit_target/` | Five small files **you fix** in Part B |
| `tests/test_security.py` | The security tests (most start red) |
| `findings-template.md` | Your findings table |
| `prompts/` | `refactor.md` and `audit-fix.md` |

---

## Part A: Refactor and Measure (15 minutes)

### Step A1: See the starting state (3 minutes)

1. In the terminal, type:

   ```bash
   bash scripts/test-refactor.sh
   ```

2. You should see `NOT YET`. **One** test fails on purpose: the performance test. It requires the new code to be much faster than the old code. The other 8 pass: they check that the answers are right.
3. Open `reconciler.py` and find `find_duplicate_ids`. It has a loop inside a loop: every transaction is compared with every other one.

*Done when:* you have seen exactly one failing test, the performance test.

### Step A2: Refactor with Copilot (8 minutes)

1. In Copilot Chat, choose **Agent** mode.
2. Attach `reconciler.py` and `prompts/refactor.md`. Type `Follow the attached prompt file.`
3. If Copilot asks to edit files, click **Allow**. If it asks to run a command, click **Skip**. Read the change, then click **Keep** if you see it.
4. Run the tests again:

   ```bash
   bash scripts/test-refactor.sh
   ```

5. One of three things happens. All three teach something:
   - **`PASSED`:** faster, and the behaviour is unchanged.
   - **An oracle or reconciler test fails:** Copilot changed the behaviour, and the safety net caught it. The most common cause is the **order** of the duplicate ids. For input `A, B, B, A`, the answer must be `["A", "B"]` (the order each id was **first** seen), not `["B", "A"]`. Tell Copilot the test name and the failure, and ask it to fix only that.
   - **Only the performance test still fails:** the code is tidier but uses the same slow algorithm. Ask Copilot: `find_duplicate_ids is still O(n^2). Make it a single pass with a Counter or a dict, keeping first-occurrence order.`
6. Read Copilot's list of "behaviours that could differ". Can you find a test for each one?

*Done when:* `bash scripts/test-refactor.sh` says `PASSED`.

*Stuck for more than 3 minutes?* `bash scripts/catch-up.sh refactor`, then `bash scripts/test-refactor.sh`.

### Step A3: Measure it (4 minutes)

1. Type:

   ```bash
   bash scripts/benchmark.sh
   ```

2. It prints how long the old and new code take. *Typical result (your numbers will differ):* `find_duplicate_ids` drops from hundreds of milliseconds to well under one, while `total_by_currency` and `count_by_status` barely change.
3. Answer in one sentence: **which change made the difference?** Only the algorithm change speeds things up. Tidier code (`Counter`, `defaultdict`) is easier to read, but it is not faster. Never claim the first kind of result for the second kind of change.

*Done when:* you can say which function got faster and why.

---

## Part B: Scan, Then Fix Two Findings (18 minutes)

### Step B1: Read, then scan (4 minutes)

1. Open the five files in `audit_target/` and read them for **2 minutes**. What worries you?
2. Run the security scanner:

   ```bash
   bash scripts/scan.sh
   ```

   It lists issues by file and line. Compare its list with yours. **One real problem does not appear in the scanner's output.** Can you find it? (Look at what `exporter.py` prints and logs.)
3. Run all the security tests once:

   ```bash
   bash scripts/test-security.sh all
   ```

   You should see `NOT YET`: 8 tests fail and 3 pass. The failures show the real damage. An injection payload returns **every** row, secrets come from the source code, a malicious payload runs its code, the same password hashes identically twice, and the full account number appears in the statement and the log.

*Done when:* you have spotted the problem the scanner missed.

### Step B2: Fix the SQL injection (7 minutes)

1. In Copilot Chat (**Agent** mode), attach `audit_target/repository.py` and `prompts/audit-fix.md`.
2. Type: `Finding: SQL injection in repository.py (the query is built by joining strings). Follow the attached prompt file.`
3. Read the change. It should pass the value as a parameter (a `?` placeholder, plus a tuple of values) instead of adding it to the SQL string. Click **Keep** if you see it.
4. Prove it:

   ```bash
   bash scripts/test-security.sh sql
   ```

*Done when:* it says `PASSED` (3 tests: a normal lookup still works, and both injection payloads are harmless).

*Stuck?* `bash scripts/catch-up.sh sql`

### Step B3: Mask the account number (7 minutes)

1. Attach `audit_target/exporter.py` and `prompts/audit-fix.md`.
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

Each one uses the same pattern: attach the file and `prompts/audit-fix.md`, describe the finding in one line, read the change, then run the test.

1. **Hard-coded secrets (`config.py`).** `Finding: hard-coded database password and API key with a silent default. Read them from the environment and raise an error if one is missing.` Prove it with `bash scripts/test-security.sh secrets`. The scanner flagged only the password. Did you notice the API key too?
2. **Unsafe `pickle` (`session_cache.py`).** `Finding: session data is loaded with pickle, so a malicious payload can run code. Use JSON and reject anything else.` Prove it with `bash scripts/test-security.sh pickle`.
3. **Weak password hashing (`auth.py`).** `Finding: passwords use a fast, unsalted hash. Use a salted, slow hash (for example hashlib.scrypt) and a constant-time comparison.` Prove it with `bash scripts/test-security.sh hash`.
4. **All green.** `bash scripts/test-security.sh all` (all 11 pass), then `bash scripts/scan.sh` again. Is it clean?
5. **Governance.** Answer the questions at the bottom of `findings-template.md`. Then write one line each: which fix would you merge on a Friday afternoon, and which would you not?

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `No such file or directory: scripts/...` | The terminal is not in the `lab` folder | `cd ~/repo/HSBC-AI-for-SE-Python-4-hours/curriculum/module-3-performance-security-and-governance/lab` |
| The performance test still fails after the refactor | Still a loop inside a loop, or the VM is very busy | Check for a single pass; rerun once. If it still fails, ask your instructor |
| An A, B, B, A test fails | The order of the duplicate ids changed | Keep first-occurrence order |
| `test_logs_never_contain_the_full_account_number` fails | Only the statement was masked | Mask the account number in the log line too |
| `No module named bandit` | The course Python environment is not active | Use the scripts, which activate it for you |
| You want to start this lab again | | `bash scripts/start-over.sh`, then type `yes`. Resets this lab folder only |

---

## Deliverables

- `bash scripts/test-refactor.sh` reports `PASSED`, and you can explain the benchmark
- `bash scripts/test-security.sh sql` and `bash scripts/test-security.sh mask` report `PASSED`
- Rows 1 and 2 of `findings-template.md`, signed by you

## Additional Resources

- Bandit documentation: <https://bandit.readthedocs.io/>
- OWASP Top 10:2025: <https://owasp.org/Top10/2025/>
- Python `timeit`: <https://docs.python.org/3/library/timeit.html>
