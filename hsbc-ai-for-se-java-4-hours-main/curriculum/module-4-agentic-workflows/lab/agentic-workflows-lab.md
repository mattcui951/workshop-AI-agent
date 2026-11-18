![General Assembly](/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Agent and PR-Review Lab

## Introduction

An **agent** is a model in a loop: it asks to use tools (run a query, read a file, propose a fix), and **your code** decides whether to run them. In this lab you run two agents that are already built:

1. A **function-calling agent** that queries a database and a price service. It asks you before every call.
2. A **PR-review agent** that finds the bug behind last night's failed job and proposes a fix. Its "human approval" step is broken: it applies the fix **without asking anyone**. You find the flaw, fix it, and prove that a declined fix never touches the code.

---

## Learning Objectives

By the end of this lab, you'll be able to:

- Run an agent, approve or decline each tool call, and read its audit trail
- Spot an approval gate that does not actually ask a human
- Fix the gate and prove it with tests and a real run
- Confirm that an approved patch repairs the failing tests

---

## How This Lab Works (read this first)

- **Time:** about 35 minutes for the core task, plus a bonus section.
- **This lab stands on its own.** You do not need anything from earlier modules.
- **The agent code is pre-built.** You change **one method** (`makeGate`, three lines).
- **The live runs need an API key.** No key? Pair with a neighbour who has one, or follow the instructor's screen. The test steps need no key.
- **Groups:** the person who runs the agent must **not** be the person who approves its patch.
- **Stuck for more than 3 minutes?** Every step has a catch-up command.

---

## Part 0: Open the Lab (2 minutes)

1. In VS Code, click **File > Open Folder...**, then go to `Desktop` > `hsbc-ai-for-se-java-4-hours-main` > `curriculum` > `module-4-agentic-workflows` > `lab`, and click **Open**. If asked, click **Yes, I trust the authors**.
2. Open a terminal: **Terminal > New Terminal**.
3. Check the key: `bash scripts/check-key.sh`. A `NO KEY` result is fine for the test steps.

| File or folder | What it is | Do you change it? |
|---|---|---|
| `ToolAgent.java` | The agent loop: asks the model, runs approved tools, keeps an audit trail | No, pre-built |
| `SafeSql.java` | Rejects any SQL that is not a plain, safe `SELECT` | No, pre-built |
| **`PrReviewApp.java`** | The PR-review agent and its approval gate | **Yes: you fix `makeGate`** |
| `sample-service/` | A small reconciliation job, **broken on purpose**, with `logs/error.log` | No. The agent fixes it |
| `src/test/`, `sample-service/src/test/` | The tests | Never edit |

All Java files are in `src/main/java/com/ga/hsbc/module4/`.

---

## Part 1: The Core Task (33 minutes)

### Step 1.1: Run the function-calling agent (6 minutes)

**What you will do:** watch an agent ask permission before each tool call.

1. Type:

   ```bash
   bash scripts/run-agent.sh
   ```

2. Each time you see `[approval gate] The agent wants to: ...`, read **what** it wants to run, then type `y` and press Enter.
3. At the end, read the **audit trail**. Every tool call is listed with `approved=true` or `approved=false`. *Typical result:* a `SELECT SUM(quantity_oz) ...` query returns 750 troy ounces, the gold price is 2321.5, and the answer is about 1,741,125 USD. The exact wording and queries can differ, because the model chooses them.
4. Run it again, and this time type `n` to every request. The audit trail shows `declined by human reviewer`, and the model is told it was refused.

*Done when:* you have seen one run where you approved and one where you declined.

*If it doesn't work:* `NO KEY` or `401`: pair with a neighbour, or watch the instructor's run, then go on to Step 2.

### Step 1.2: See the overnight failure (4 minutes)

1. Run the broken service's own tests:

   ```bash
   bash scripts/test-service.sh
   ```

   You should see `NOT YET`: 3 tests fail and 2 pass.
2. Open `sample-service/logs/error.log`. Then open `sample-service/src/main/java/com/atlas/recon/BusinessDays.java`.
3. **Before any agent runs,** write your own guess in one sentence: which method is the root cause, and why does a **Monday** run fail? (Hint: what is the "previous business day" before a Monday?)

*Done when:* you have written your own hypothesis.

### Step 1.3: Run the PR-review agent and catch the flaw (6 minutes)

1. Type:

   ```bash
   bash scripts/run-pr-review.sh
   ```

2. Watch what the agent is **able** to do: list files, read the source, read the log, and propose a patch. It shows `--- Proposed change (NOT applied yet) ---` and a diff.
3. Now watch the next line. It says **`Applied.`**. Nobody was asked. That is the flaw.
4. Check:

   ```bash
   bash scripts/service-status.sh
   ```

   It says `CHANGED`. The agent changed the code without a human decision.
5. Open `PrReviewApp.java` and find `makeGate`, with the comment marked `TODO (lab)`. It says `return (action, details) -> true;`. In plain English, that means "say yes to everything". Write one sentence: why is this not a human gate?
6. Put the service back the way it was:

   ```bash
   bash scripts/restore-service.sh
   ```

*Done when:* `bash scripts/service-status.sh` says `UNCHANGED`, and you have written your sentence.

### Step 1.4: Fix the gate (8 minutes)

1. See the gate's tests fail:

   ```bash
   bash scripts/test-gate.sh
   ```

   You should see `NOT YET`: 3 of 4 tests fail.
2. In Copilot Chat (**Agent** mode), attach `PrReviewApp.java` and `prompts/you-do-gate.md`. Type `Follow the attached prompt file.`
3. Read the change. The fixed rule is: **reading and proposing run freely; anything else, including applying the patch, asks the human.** It should look roughly like this:

   ```java
   return (action, details) -> SAFE_TOOLS.contains(action) || human.approve(action, details);
   ```

   Check that Copilot changed **only** `makeGate`. Click **Keep** if you see it.
4. Run `bash scripts/test-gate.sh` again.

*Done when:* `bash scripts/test-gate.sh` says `PASSED`.

*Stuck?* `bash scripts/catch-up.sh gate`, then `bash scripts/test-gate.sh`.

### Step 1.5: Prove it: say no, then say yes (9 minutes)

1. Run the PR-review agent again: `bash scripts/run-pr-review.sh`. This time it **stops and asks** `Approve? (y/n)` before applying the patch. Read the diff. Type **`n`**.
2. Check that nothing changed:

   ```bash
   bash scripts/service-status.sh
   ```

   It must say `UNCHANGED`. **This is the most important check in the lab:** a declined patch never touches the code.
3. Run the agent once more. Read the diff as a reviewer. Does it fix the **root cause** (Saturday is not a business day), or does it only hide the symptom (for example, catching the exception)? If it fixes the root cause, type **`y`**.
4. Prove the fix:

   ```bash
   bash scripts/test-service.sh
   ```

   All 5 tests should pass. `bash scripts/service-status.sh` now says `CHANGED` and shows the `.bak` backup of the original.

*Done when:* the declined run left the service `UNCHANGED`, and after the approved run `bash scripts/test-service.sh` says `PASSED`.

*If it doesn't work:*

- **The agent's patch did not fix the tests:** that happens, because the model is not perfect. Run `bash scripts/restore-service.sh`, then run the agent again, and type `n` to any patch that does not fix the root cause. Or apply the reference fix: `bash scripts/catch-up.sh service`.
- **No key:** apply the reference fix with `bash scripts/catch-up.sh service`, then run `bash scripts/test-service.sh`.

---

## You Finished the Core Task

You caught an approval gate that approved everything, fixed it, and proved that a "no" leaves the code untouched while a "yes" repairs it. The key idea: **the model only asks; your code decides, and a human decides the risky part.**

---

## Bonus: Keep Going (on your own or if you finish early)

1. **Try to beat the SQL check (Ask mode).** Attach `SafeSql.java`, `ToolAgent.java` and `prompts/we-do-tools.md`. Which rule would you have forgotten? (`information_schema` is a common miss.) Which control still protects the database if the check misses something? (The agent connects with a read-only database account.)
2. **The second, hidden bug.** After the fix, what does the job do on a day with **no trades at all**, such as a holiday? Read `ReconciliationJob.averageNotionalCents`. Ask Copilot (Ask mode) to propose a fix and a test for it. Do not apply it.
3. **Run every test.** First put the service back with `bash scripts/restore-service.sh` (some agent tests need the original, broken service). Then run `bash scripts/test-all.sh`. With the gate fixed, everything should pass. (One test may say *skipped* if this machine has no `solutions` folder. That is expected.)
4. **Discuss with a neighbour:** why does the gate let reads and proposals through without asking? When would you want a human to approve even a *read*? What else would you add before trusting this on a real repository? (For example: who approved what, running the tests before and after, limits on patch size.)

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `No such file or directory: scripts/...` | The terminal is not in the `lab` folder | `cd ~/repo/HSBC-AI-for-SE-Java-4-hours/curriculum/module-4-agentic-workflows/lab` |
| `[approval] AUTO_APPROVE set` | An environment setting skips the human | The scripts clear it for you. Run the agent with the scripts only |
| The agent ran but proposed no patch | The model did not finish in 10 steps | Run it again |
| `Applied.` without being asked, after your fix | The fix was not saved, or did not compile | **File > Save All**, then `bash scripts/test-gate.sh` |
| `ArithmeticException: / by zero` in the service tests | The original bug (Saturday has no trades) | This is expected before the fix |
| You want to start this lab again | | `bash scripts/start-over.sh`, then type `yes`. Resets this lab folder, including `sample-service` |

---

## Deliverables

- One sentence on why `return (action, details) -> true;` is not a human gate
- `bash scripts/test-gate.sh` reports `PASSED`
- A declined run that left the service `UNCHANGED`, and an approved run after which `bash scripts/test-service.sh` reports `PASSED`

## Additional Resources

- Anthropic tool use overview: <https://docs.claude.com/en/docs/agents-and-tools/tool-use/overview>
- OWASP Top 10 for Large Language Model Applications: <https://owasp.org/www-project-top-10-for-large-language-model-applications/>
- H2 Database documentation: <https://h2database.com/html/main.html>
