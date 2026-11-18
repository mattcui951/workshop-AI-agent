![General Assembly](/curriculum/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# RAG Question-Answering Service Lab

## Introduction

Engineers keep re-reading the same operations documents to answer basic questions. In this lab you finish a small Python service that answers questions **only from three internal documents**. It refuses when the documents do not cover the question. A human must approve every answer before anyone sees it.

The search half (`ingest.py`: splitting documents into chunks, scoring them, finding the best ones) and the review queue are **already built for you**. You write the two pieces that talk to the model safely, then act as the human reviewer.

---

## Learning Objectives

By the end of this lab, you'll be able to:

- Build a prompt that treats retrieved text as data, so a document cannot break out of it
- Build a service that declines, without calling the model, when nothing relevant is found
- Act as the human reviewer: release or reject a draft answer by checking it against its sources

---

## How This Lab Works (read this first)

- **Time:** about 35 minutes for the core task, plus a bonus section.
- **This lab stands on its own.** You do not need anything from Module 1.
- **Stuck for more than 3 minutes?** Every step has a catch-up command. Use it and keep going.
- **Commands are short scripts** in the `scripts/` folder. Type them exactly. Press **Tab** to auto-complete names.
- **"Red" tests are normal.** A red (failing) test is a to-do item, not a mistake. Your job is to make the tests green.

---

## Part 0: Open the Lab (3 minutes)

1. In VS Code, click **File > Open Folder...**, then go to `repo` > `HSBC-AI-for-SE-Python-4-hours` > `curriculum` > `module-2-rag-pipelines-and-llm-apis` > `lab`, and click **Open**. If asked, click **Yes, I trust the authors**, and choose the `ga-python-venv` interpreter.
2. Open a terminal: **Terminal > New Terminal**.
3. Check whether you have an API key for the live demo:

   ```bash
   bash scripts/check-key.sh
   ```

   - `KEY FOUND`: you can do the live demo in Step 3.
   - `NO KEY`: you can still do Steps 1 and 2. For Step 3, watch the instructor's screen or a neighbour's.
4. Check Copilot is signed in: the Copilot icon in the bottom Status Bar shows no warning. (Not signed in? See Module 1, Step 0.3.)

### What is in this folder

| File or folder | What it is | Do you change it? |
|---|---|---|
| `sample_docs/` | Three made-up operations documents: the "knowledge" | No |
| `ingest.py` | Splits, scores and searches the documents | No, already built |
| **`qa_service.py`** | **Top half:** `build_prompt` and `RagService.ask`. **Bottom half:** the review queue | **Yes: you write `build_prompt` and `ask`.** The review queue is already built |
| `llm.py`, `ask_cli.py` | The Claude client and the command-line demo | No |
| `tests/` | The tests: the specification | Never edit |
| `prompts/` | Prompt files to attach in Copilot | No |
| `scripts/` | Short commands you run | No |

---

## Part 1: The Core Task (32 minutes)

### Step 1: See where you are starting (3 minutes)

**What you will do:** run the tests, so you know what is already done and what is yours to do.

1. In the terminal, type:

   ```bash
   bash scripts/test-all.sh
   ```

2. You should see `NOT YET` and, near the bottom, `5 failed, 22 passed`. All 5 `FAILED` lines are in `tests/test_qa_service.py` and say `NotImplementedError: TODO: write build_prompt` or `TODO: write RagService.ask`: those are your work. The other 22 already pass, because that code is finished. (A long red traceback above the summary is normal here.)
3. Open `qa_service.py`. Find `build_prompt` and `RagService.ask`, each with `raise NotImplementedError`. Read the docstring above each one. **That docstring is the specification.** Copilot will follow it.

*Done when:* you have seen which tests fail, and you have read the two docstrings.

### Step 2: Write `build_prompt` and `ask` with Copilot (14 minutes)

**What you will do:** have Copilot write two functions, then make the tests green.

1. In Copilot Chat, choose **Agent** mode.
2. Attach `qa_service.py`, `tests/test_qa_service.py` and `prompts/you-do-qa-service.md` (drag them into the chat, or type `#` and the name).
3. Type `Follow the attached prompt file.` and press Enter.
4. If Copilot asks to edit files, click **Allow** or **Continue**. If it asks to run a terminal command, click **Skip**. You run the tests.
5. Look at the change, then click **Keep** if you see it. Check that it changed **only** `build_prompt` and `ask`, not the review queue at the bottom and not `ingest.py`. If it touched anything else, click **Undo** for that part.
6. Run the tests:

   ```bash
   bash scripts/test-core.sh
   ```

7. If you see `NOT YET`, read the first `FAILED` line and the `assert` under it. In Copilot Chat, type `The test <test name> fails with: <what it said>. Fix only that.` Run `bash scripts/test-core.sh` again.
8. When it says `PASSED`, read what Copilot told you about the **poisoned document**: a document that tries to type `</source>` to break out of its box. Write one sentence: how does `build_prompt` stop that? (Hint: it escapes `<` and `>`.)

*Done when:* `bash scripts/test-core.sh` says `PASSED`.

*If it doesn't work:*

- **A `SyntaxError` or `IndentationError`:** the error names the line. Ask Copilot to "fix only the syntax error in qa_service.py".
- **`test_an_off_topic_question_is_declined_without_calling_the_model` fails:** `ask` must **not** call the model when the best score is below `min_score`. Ask Copilot to re-read that rule in the `ask` docstring.
- **Stuck for more than 3 minutes:** run `bash scripts/catch-up.sh`, then `bash scripts/test-core.sh`.

### Step 3: Be the human reviewer (15 minutes)

**What you will do:** ask real questions, read each draft answer next to its sources, and decide whether to release it. **This is the most important part of the lab.**

> **No key?** Pair with a neighbour who has one, or watch the instructor's screen. You still make the release or reject decisions yourself.

1. Ask the first question:

   ```bash
   bash scripts/demo.sh What time is the cutoff for same-day settlement?
   ```

2. Read the output from top to bottom:
   - **Top retrieved chunks:** the three pieces of the documents that scored highest. A score near 0.45 is a strong match here.
   - **Draft answer:** what the model wrote **using only those chunks**. Look for citations like `[1]`.
   - **Reviewer name:** type your name and press Enter. Do not leave it blank: a blank name is refused, which is the point.
   - **Approve this answer? (y/n):** type `y` to release it, or `n` to reject it.
3. Do the same for the next two questions. Before you approve, check every fact in the draft against the chunks above it:

   ```bash
   bash scripts/demo.sh Can a failed payment be retried with the same payment id?
   bash scripts/demo.sh What is the high-value limit for stuck payments?
   ```

4. Open `review-decisions.md` (it is already in the lab folder) and fill in one row per question: **did you approve or reject it, and why.** Save it with **Ctrl+S**. Use this table to know what to check:

| Question | What a good answer does |
|---|---|
| Cutoff for same-day settlement | Gives 2:00 PM **local market time**. Reject a draft that adds a time zone the document does not give |
| Retry a failed payment with the same id? | Says **no**: a failed payment is final and must be re-submitted as a **new** payment |
| The high-value limit | The handbook mentions "the high-value limit" but **never gives a number**. A good answer says it does not know. Reject any draft that invents one |

*Done when:* `review-decisions.md` has an approve or reject decision, with a reason, for all three questions.

*If it doesn't work:*

- **`(Set ANTHROPIC_API_KEY to run the answer step.)`:** you have no key. Retrieval still worked, so look at the chunks and decide what a correct answer would say.
- **`ValueError: A named reviewer is required`:** you left the reviewer name blank. Run the command again and type your name.
- **`401` or `authentication_error`:** the key is wrong. Tell your instructor.
- **`credit balance is too low`:** the key's account has no credit. Tell your instructor.
- **`NotImplementedError`:** Step 2 is not finished. Run `bash scripts/catch-up.sh` first, then the demo.

---

## You Finished the Core Task

You wrote the two functions that keep a document-grounded assistant honest, and you acted as the human gate. The key idea: **the model is a reader of your documents, not a source of facts**, and a person decides what is released.

---

## Bonus: Keep Going (on your own or if you finish early)

1. **One more review decision.** Run `bash scripts/demo.sh What is the best recipe for banana bread?`. It should be declined **before** the model is called, so no reviewer step appears. Why is that cheaper and safer?
2. **Fact-check with Copilot (Ask mode).** Attach `prompts/assess-answers.md`. Describe a draft answer you saw and its sources, in your own words. Does Copilot agree with your decision?
3. **Understand the search half (Ask mode).** Attach `ingest.py` and `prompts/we-do-ingest.md`. Why is this not a real embedding model? Why `md5` and not Python's `hash()`?
4. **Move the threshold.** In `qa_service.py`, `MIN_RELEVANCE` is `0.20`. Change it to `0.35`, run `bash scripts/test-all.sh` and the demo, and see which on-topic questions are now refused. Change it back to `0.20`.
5. **Add a document.** Add a short `.txt` file of your own to `sample_docs/` and re-run your questions. Did the scores change? Would you change `MIN_RELEVANCE`? (Delete the file afterwards, or run `bash scripts/start-over.sh`.)

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `No such file or directory: scripts/...` | The terminal is not in the lab folder | `cd ~/repo/HSBC-AI-for-SE-Python-4-hours/curriculum/module-2-rag-pipelines-and-llm-apis/lab` |
| `NotImplementedError: TODO: write ...` | A function is still the starter version | Do Step 2, or run `bash scripts/catch-up.sh` |
| Tests in `tests/test_ingest.py` start failing | Copilot changed `ingest.py` | Undo that change, or `bash scripts/start-over.sh` |
| `No module named anthropic` (or `pydantic`) | The course Python environment is not active | Use the scripts, which activate it for you |
| You want to start this lab again | | `bash scripts/start-over.sh`, then type `yes`. Resets this lab folder only |

---

## Deliverables

- `bash scripts/test-core.sh` reports `PASSED` (and `bash scripts/test-all.sh` too)
- `review-decisions.md` filled in: an approve or reject decision, with a one-line reason, for the three review questions

## Additional Resources

- Anthropic prompt engineering overview: <https://docs.claude.com/en/docs/build-with-claude/prompt-engineering/overview>
- OWASP Top 10 for Large Language Model Applications: <https://owasp.org/www-project-top-10-for-large-language-model-applications/>
