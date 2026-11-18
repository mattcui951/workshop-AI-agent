![General Assembly](/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Java RAG Question-Answering Service Lab

## Introduction

Engineers keep re-reading the same operations documents to answer basic questions. In this lab you finish a small Java service that answers questions **only from three internal documents**. It refuses when the documents do not cover the question. A human must approve every answer before anyone sees it.

The search half (splitting documents into chunks, scoring them, finding the best ones) and the review queue are **already built for you**. You write the two pieces that talk to the model safely, then act as the human reviewer.

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
- **Groups:** one person types, one reviews what Copilot wrote, and you swap at each step.

---

## Part 0: Open the Lab (3 minutes)

1. In VS Code, click **File > Open Folder...**, then go to `Desktop` > `hsbc-ai-for-se-java-4-hours-main` > `curriculum` > `module-2-java-rag-pipelines` > `lab`, and click **Open**. If asked, click **Yes, I trust the authors**.
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
| `sample_docs/` | Three made-up bullion-desk documents: the "knowledge" | No |
| `Chunker`, `Embedder`, `InMemoryIndex` | Split, score and search the documents | No, already built |
| `ReviewQueue` | Holds answers until a named reviewer approves them | No, already built |
| **`PromptBuilder.java`** | Builds the text sent to the model | **Yes: you write `build`** |
| **`RagService.java`** | Searches, decides, calls the model | **Yes: you write `ask`** |
| `src/test/...` | The tests: the specification | Never edit |
| `prompts/` | Prompt files to attach in Copilot | No |
| `scripts/` | Short commands you run | No |

All Java files are in `src/main/java/com/ga/hsbc/module2/`.

---

## Part 1: The Core Task (32 minutes)

### Step 1: See where you are starting (3 minutes)

**What you will do:** run the tests, so you know what is already done and what is yours to do.

1. In the terminal, type:

   ```bash
   bash scripts/test-all.sh
   ```

2. You should see `NOT YET`, and near the bottom `Tests run: 32, Failures: 0, Errors: 5`. The 5 errors all say `UnsupportedOperationException: TODO: write PromptBuilder.build` or `TODO: write RagService.ask`: those are your work. The other 27 already pass, because that code is finished. (A long list of `[ERROR]` lines is normal here.)
3. Open `PromptBuilder.java` and `RagService.java`. Find the comments that start with `YOUR JOB` and the description just above `build` and `ask`. **That description is the specification.** Copilot will follow it.

*Done when:* you have seen exactly which tests fail, and you have read the two `YOUR JOB` comments.

### Step 2: Write `build` and `ask` with Copilot (14 minutes)

**What you will do:** have Copilot write two methods, then make the tests green.

1. In Copilot Chat, choose **Agent** mode.
2. Attach these four files (drag them into the chat, or type `#` and the name): `PromptBuilder.java`, `RagService.java`, `PromptBuilderTest.java` and `RagServiceTest.java`. Also attach `prompts/you-do-qa-service.md`.
3. Type `Follow the attached prompt file.` and press Enter.
4. If Copilot asks to edit files, click **Allow** or **Continue**. If it asks to run a terminal command, click **Skip**. You run the tests.
5. Look at each change, then click **Keep** if you see it. Check that it changed **only** `build` and `ask`. If it touched any other file, click **Undo** on that file.
6. Run the tests for your two methods:

   ```bash
   bash scripts/test-core.sh
   ```

7. If you see `NOT YET`, read the first `[ERROR]` line. It names the test and what was expected. In Copilot Chat, type: `The test <test name> fails with: <what it said>. Fix only that.` Run `bash scripts/test-core.sh` again.
8. When it says `PASSED`, read what Copilot told you about the **poisoned document**: a document that tries to type `</source>` to break out of its box. Write one sentence: how does `build` stop that? (Hint: it escapes `<` and `>`.)

*Done when:* `bash scripts/test-core.sh` says `PASSED`.

*If it doesn't work:*

- **`COMPILATION ERROR`:** the error names a file and line. Ask Copilot to "fix only the compile error in RagService.java".
- **`anOffTopicQuestionIsDeclinedWithoutCallingTheModel` fails:** `ask` must **not** call the model when the best score is below the threshold. Ask Copilot to re-read that rule in the `ask` description.
- **Stuck for more than 3 minutes:** run `bash scripts/catch-up.sh core`, then `bash scripts/test-core.sh`.

### Step 3: Be the human reviewer (15 minutes)

**What you will do:** ask real questions, read each draft answer next to its sources, and decide whether to release it. **This is the most important part of the lab.**

> **No key?** Pair with a neighbour who has one, or watch the instructor's screen. You still make the release or reject decisions yourself.

1. Ask the first question:

   ```bash
   bash scripts/demo.sh What time is the cutoff for same-day settlement?
   ```

2. Read the output from top to bottom:
   - **Top retrieved chunks:** the three pieces of the documents that scored highest. A score near 0.5 is a strong match.
   - **Draft answer:** what the model wrote **using only those chunks**. Look for citations like `[1]`.
   - **Reviewer name:** type your name and press Enter. Do not leave it blank: a blank name is refused, which is the point.
   - **Approve this answer? (y/n):** type `y` to release it, or `n` to reject it.
3. Do the same for the next two questions. Before you approve, check every fact in the draft against the chunks shown above it:

   ```bash
   bash scripts/demo.sh What is the cutoff for same-day settlement of silver in London?
   bash scripts/demo.sh Does a custom confirmation template need only business sign-off?
   ```

4. Open `review-decisions.md` (it is already in the lab folder) and fill in one row per question: **did you approve or reject it, and why.** Save it with **Ctrl+S**. Use this table to know what to check:

| Question | What a good answer does |
|---|---|
| Cutoff for same-day settlement | Gives the right time **and** says it is New York time |
| Silver in London | The documents give only a New York time. A good answer says it **does not know** a London cutoff. Reject any draft that invents a London time |
| Custom template, only business sign-off? | The rules also require **legal** sign-off. A draft that leaves out legal is wrong: reject it |

*Done when:* `review-decisions.md` has an approve or reject decision, with a reason, for all three questions.

*If it doesn't work:*

- **`(Set ANTHROPIC_API_KEY to run the answer step.)`:** you have no key. Retrieval still worked, so look at the chunks and decide what a correct answer would say.
- **`IllegalArgumentException: A named reviewer is required`:** you left the reviewer name blank. Run the command again and type your name.
- **`401` or `authentication_error`:** the key is wrong. Tell your instructor.
- **`credit balance is too low`:** the key's account has no credit. Tell your instructor.
- **The tests fail and you are out of time:** run `bash scripts/catch-up.sh core` first, then the demo.

---

## You Finished the Core Task

You wrote the two methods that keep a document-grounded assistant honest, and you acted as the human gate. The key idea: **the model is a reader of your documents, not a source of facts**, and a person decides what is released.

---

## Bonus: Keep Going (on your own or if you finish early)

1. **Two more review decisions.** Run `bash scripts/demo.sh How many repeated failures with the same counterparty trigger an escalation?` The policy says "repeated failures" and gives **no number**: reject any draft that invents one. Then try `bash scripts/demo.sh What is the best recipe for banana bread?`. It should be declined **before** the model is called, so no reviewer step appears.
2. **Fact-check with Copilot (Ask mode).** Attach `prompts/assess-answers.md`. Describe a draft answer you saw and its sources, in your own words. Does Copilot agree with your decision?
3. **Understand the search half (Ask mode).** Attach `Chunker.java`, `Embedder.java`, `InMemoryIndex.java` and `prompts/we-do-ingest.md`. Why is this not a real embedding model? What would a production system use instead?
4. **Move the threshold.** In `RagApp.java`, `MIN_RELEVANCE` is `0.20`. Change it to `0.35`, run `bash scripts/test-all.sh` and the demo, and see which on-topic questions are now refused. Change it back to `0.20`.
5. **Put it behind a web endpoint (needs a key).** Attach `prompts/endpoint.md`, `RagService.java` and `ReviewQueue.java` in **Agent** mode. Copilot creates `web/RagServer.java`. (Short on time? Run `bash scripts/catch-up.sh server`.) Start it with `bash scripts/start-server.sh`. Open a **second** terminal (the **+** in the terminal panel) and type, one at a time:

   ```bash
   bash scripts/ask.sh What time is the cutoff for same-day settlement?
   bash scripts/answer.sh A-0001
   bash scripts/reviews.sh
   bash scripts/approve.sh A-0001 your.name
   bash scripts/answer.sh A-0001
   ```

   The first `answer.sh` is refused (`403`), because nothing is approved yet. After `approve.sh`, the answer is released. Stop the server with **Ctrl+C** in the first terminal.

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `No such file or directory: scripts/...` | The terminal is not in the lab folder | `cd ~/repo/HSBC-AI-for-SE-Java-4-hours/curriculum/module-2-java-rag-pipelines/lab` |
| `UnsupportedOperationException: TODO: write ...` | A method is still the starter version | Do Step 2, or run `bash scripts/catch-up.sh core` |
| Tests in `ChunkerTest` or `ReviewQueueTest` fail | Copilot changed a pre-built file | Undo that change, or run `bash scripts/start-over.sh` |
| `Address already in use` (bonus 5) | The server is already running in another terminal | Stop it with Ctrl+C there, then start again |
| `Could not resolve` or `Could not transfer artifact` | VM Maven problem, not your code | Tell your instructor |
| You want to start this lab again | | `bash scripts/start-over.sh`, then type `yes`. Resets this lab folder only |

---

## Deliverables

- `bash scripts/test-core.sh` reports `PASSED` (and `bash scripts/test-all.sh` too)
- `review-decisions.md` filled in: an approve or reject decision, with a one-line reason, for the three review questions

## Additional Resources

- Anthropic prompt engineering overview: <https://docs.claude.com/en/docs/build-with-claude/prompt-engineering/overview>
- OWASP Top 10 for Large Language Model Applications: <https://owasp.org/www-project-top-10-for-large-language-model-applications/>
- Eclipse Vert.x documentation: <https://vertx.io/docs/>
