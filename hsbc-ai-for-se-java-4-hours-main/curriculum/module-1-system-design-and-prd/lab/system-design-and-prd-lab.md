![General Assembly](/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# PRD-to-Contract Lab

## Introduction

A product owner has handed you a short, vague requirements document (a PRD) for a **Single Cash Flow Trade Service**. Before anyone writes real code, you will use GitHub Copilot to find what the PRD leaves out. Then you will turn a finished API specification into Java code and **prove with a test** that the two agree.

This is also the lab where you set up your tools for the day: the lab VM, VS Code and GitHub Copilot.

---

## Learning Objectives

By the end of this lab, you'll be able to:

- Sign in to GitHub Copilot in VS Code and attach files to a chat
- Use Copilot to find the most important gaps in a PRD, and write down your own default for each
- Generate Java records from an OpenAPI specification
- Prove with a contract test that the code and the specification agree, and read the failure when they do not

---

## How This Lab Works (read this first)

- **Time:** about 35 minutes for the core task. A bonus section at the end has more to do if you finish early.
- **You will not be left behind.** Every step says what "done" looks like. If you are stuck for more than 3 minutes, use the **catch-up** command in that step and keep going.
- **Everything you need is already in the lab folder.** The OpenAPI spec (`openapi.yaml`) is written for you, so you can spend your time on the parts that matter.
- **You type commands, you do not paste them.** Copy and paste into the lab VM may not work. Commands are kept short. Press **Tab** to auto-complete file and folder names.
- **Save your Copilot messages.** Copilot Free has a limited monthly allowance. Each step needs only one or two chat messages, so use them for the steps below.
- **Work alone or in a group of up to four** sharing one screen: one person types, one person reviews what Copilot wrote, and you swap at each step.

---

## Part 0: Set Up Your Tools (10 minutes)

### Step 0.1: Get into your lab VM (Skillable)

Your lab VM is a Linux computer that runs in your web browser. It is provided by **Skillable**.

1. Open the Skillable link your instructor gave you and sign in.
2. Click **Launch** (or **Start**) for this course's lab. It can take a minute or two to start.
3. When you see a Linux desktop in your browser, you are in. If it asks you to log in, use the details on the Skillable page or from your instructor.

**Good to know:**

- Click inside the VM screen before you type, so your keys go to the VM and not your own computer.
- To make the VM bigger, use the full-screen or "fit to window" button in the Skillable toolbar.
- If the VM screen freezes, refresh your browser tab. The VM keeps running, and nothing is lost.
- **Do not click "End" or "Submit"** in Skillable until your instructor says so. Ending the lab can delete your VM and your work.

*Done when:* you can see the Linux desktop and click on things inside it.

### Step 0.2: Open this lab in VS Code

You will open **only this lab's folder**, not the whole course. That keeps Copilot focused on the right files.

1. Open **VS Code** from the desktop or the app menu.
2. Click **File > Open Folder...**
3. Go to `Desktop` > `hsbc-ai-for-se-java-4-hours-main` > `curriculum` > `module-1-system-design-and-prd` > `lab`, then click **Open**. (On the course VM the full path is `~/Desktop/hsbc-ai-for-se-java-4-hours-main/curriculum/module-1-system-design-and-prd/lab`. Your instructor will tell you if yours is different.)
4. If VS Code asks **"Do you trust the authors of the files in this folder?"**, click **Yes, I trust the authors**.
5. Open a terminal inside VS Code: click **Terminal > New Terminal**. A terminal panel appears at the bottom. It is already in the lab folder.
6. In the terminal, type this and press Enter:

   ```bash
   java -version && mvn -v
   ```

*Done when:* the terminal shows `openjdk version "21...` and `Apache Maven 3.9...`, and the Explorer panel on the left shows `openapi.yaml`, `prd`, `prompts` and `scripts`.

*If it doesn't work:*

- **The Explorer shows the whole course, not just `lab`:** you opened the wrong folder. Repeat step 2 and pick the `lab` folder itself.
- **`command not found`:** tell your instructor. The VM image is missing a tool.

### Step 0.3: Sign in to GitHub Copilot (Copilot Free)

You will use **your own GitHub account** with the free Copilot plan.

1. **No GitHub account yet?** In the VM's web browser, go to `github.com/signup` and create one. It takes about 2 minutes. Keep your phone nearby in case GitHub sends you a code.
2. In VS Code, look at the **Status Bar**, the thin bar along the bottom of the window. Find the **Copilot icon** (a small face with goggles). Hover over it or click it.
3. Click **Use AI Features** (it may also say **Sign in to use Copilot**).
4. Choose **Continue with GitHub**. A web browser opens in the VM.
5. Sign in to GitHub. If GitHub asks for a code from your phone or authenticator app, enter it.
6. Click **Authorize Visual Studio Code**. If the browser asks to open VS Code, click **Open**.
7. Back in VS Code, if you have no Copilot plan yet, you are signed up for **Copilot Free** automatically. Accept any prompt that asks you to start it.

*Done when:* the Copilot icon in the Status Bar no longer shows a warning, and you can open the chat (next step).

*If it doesn't work:*

- **The browser did not come back to VS Code:** go back to VS Code yourself. If it is still not signed in, click the Copilot icon again. Some versions show a short code to type at `github.com/login/device`.
- **"You do not have access to Copilot":** in the VM browser, open `github.com/settings/copilot` and start Copilot Free, then reload VS Code (**View > Command Palette**, type `Reload Window`, press Enter).
- **Still stuck after 3 minutes:** pair with a neighbour for this lab and tell your instructor. You can do every step by looking at their screen.

### Step 0.4: Open Copilot Chat and learn three moves

1. Open the chat: press **Ctrl+Alt+I**, or click the chat (speech bubble) icon at the top of the VS Code window.
2. **Pick the mode.** At the bottom of the chat box is a mode picker. You will use two modes:
   - **Ask:** Copilot answers in the chat. It changes no files. Use it for reading and reviewing.
   - **Agent:** Copilot can create and edit files for you. Use it when a step says "create".
3. **Attach a file.** Do this instead of typing or pasting long text. Any of these works:
   - drag the file from the Explorer panel into the chat box, or
   - click **Add Context** (the paperclip) and pick the file, or
   - type `#` in the chat box and start typing the file name, then pick it from the list.

   An attached file shows as a small tag above the chat box.
4. **Keep or undo.** When Copilot (in Agent mode) changes files, it writes the changes to disk straight away and lists the changed files in the chat, usually in a box above the chat input that says something like "2 files changed", with **Keep** and **Undo** buttons. The changed file may also open with the new lines highlighted and its own **Keep** / **Undo** buttons. Read the change first, then click **Keep** to accept it, or **Undo** to throw it away.
   - **No Keep button anywhere?** That is fine. Some VS Code versions, or the setting that auto-accepts edits, apply the change without asking. Check the file exists in the Explorer and looks right, then carry on. To throw a change away instead, click **Undo** in the chat, or ask Copilot to undo it.

**About `copilot-instructions.example.md`:** this file is **for reference only**. You do not need to copy it or attach it. A real copy is already installed in this lab at `.github/copilot-instructions.md`, and Copilot reads it automatically. It tells Copilot the team rules, for example: use `BigDecimal` for money, never Spring, and do not run terminal commands. At work, you would put a file like this at the root of your own repository.

*Done when:* you can see the chat box and the mode picker.

---

## Part 1: The Core Task (25 minutes)

### Step 1.1: Find the three gaps that matter most (8 minutes)

**What you will do:** read the PRD, ask Copilot for its top five gaps, and write down your three most important ones with a default for each. Do not try to fix every gap. Real sprint planning picks the few that block work.

1. In the Explorer, open `prd/single-cash-flow-prd.md`. Read it yourself for **2 minutes**. Notice anything vague, for example: what is an "ounce"? What is a "business day"? Who is allowed to cancel?
2. In Copilot Chat, choose **Ask** mode.
3. Attach two files: `prd/single-cash-flow-prd.md` and `prompts/03-find-the-gaps.md`.
4. In the chat box, type: `Follow the attached prompt file.` and press Enter.
5. Read Copilot's five gaps. Compare them with what you noticed.
6. Open `assumptions.md` (it is already in the lab folder). Fill in **three rows**: the requirement, the question you would ask, and your default. Use your own words. You do not have to agree with Copilot.
7. Save the file (**Ctrl+S**).

*Done when:* `assumptions.md` has three filled rows and one line saying which is riskiest.

*Watch out:* Copilot often states made-up numbers ("500 ms", "24 hours", "99.9%") as if they were facts. They are only suggestions. In real work, you would take them to the product owner as questions.

*If it doesn't work:*

- **Copilot replied with code, or with 15 gaps:** check that you attached `prompts/03-find-the-gaps.md` and that the mode is **Ask**. Then type `Only the five most important, one line each.`
- **Out of Copilot messages:** skip the chat and fill in `assumptions.md` from your own reading. That is the real skill.

### Step 1.2: Generate the Java records from the spec (10 minutes)

**What you will do:** ask Copilot to turn `openapi.yaml` (already written for you) into five small Java files.

1. Open `openapi.yaml` and scroll to `components: schemas:` near the bottom. You will see three schemas: `CreateCashFlowTradeRequest`, `CashFlowTradeResponse` and `ErrorResponse`. Each has a list of properties. Your Java records must use **exactly these names**.
2. In Copilot Chat, switch to **Agent** mode.
3. Attach `openapi.yaml` and `prompts/05-records.md`.
4. Type `Follow the attached prompt file.` and press Enter.
5. If Copilot asks for permission to create or edit files, click **Allow** or **Continue**. If it asks to run a terminal command, click **Skip** or **Cancel**. You will run the tests yourself, and skipping saves Copilot messages.
6. Copilot creates five files in `src/main/java/com/ga/hsbc/module1/`. Open each one to read it. Then click **Keep** in the chat (or in the file) if you see it. No Keep button? The files are already saved: just check they are there.
7. **Review like a senior engineer**, about one minute per file. Check:
   - Money and quantities use `BigDecimal`, **not** `double`.
   - There are **no Spring annotations**, such as `@RestController` or `@Valid`.
   - `Metal` has GOLD, SILVER, PLATINUM, PALLADIUM. `TradeStatus` has NEW, CONFIRMED, SETTLED, CANCELLED.
   - The record constructor rejects bad input, such as a blank client or a negative quantity.

*Done when:* the folder `src/main/java/com/ga/hsbc/module1/` has `Metal.java`, `TradeStatus.java`, `CreateCashFlowTradeRequest.java`, `CashFlowTradeResponse.java` and `ErrorResponse.java`.

*If it doesn't work:*

- **The files went to the wrong folder or have different names:** type in chat: `Move the files to src/main/java/com/ga/hsbc/module1/ and use exactly the names in the prompt.`
- **No Agent mode, or Copilot only showed code in the chat:** hover over each code block in the chat and click **Insert into New File** (or the "..." menu), then save it with the right name in the right folder.
- **Stuck for more than 3 minutes:** type `bash scripts/catch-up.sh` in the terminal. It copies in finished records, so you can do Step 3.

### Step 1.3: Prove the code and the spec agree (7 minutes)

**What you will do:** run a contract test that compares your Java records with `openapi.yaml`, then break it on purpose to see what it catches.

1. In the terminal, type:

   ```bash
   bash scripts/check-contract.sh
   ```

2. Read the result:
   - **`PASSED`:** your records match the spec. Go to step 3.
   - **`NOT YET`:** read the `[ERROR]` lines. Each one says what is wrong, for example `CashFlowTradeResponse is missing property tradeId`, or `class ... ErrorResponse not found`. Fix the name in the Java file (or ask Copilot: attach the file and paste nothing, just type `The contract test says: <what it said>. Fix only that.`). Run the check again.
3. **Break it on purpose.** Open `openapi.yaml`, find `clientId:` under `CreateCashFlowTradeRequest`, and change it to `clientRef:`. Save. Run `bash scripts/check-contract.sh` again.
4. Read the failure. It names the exact property that no longer matches. **This is the point of the lab:** code generated by AI only agrees with the spec by luck, unless a test checks it.
5. Change `clientRef:` back to `clientId:`, save, and run the check once more. It should say `PASSED`.

*Done when:* `bash scripts/check-contract.sh` says `PASSED`, and you have seen it fail when the spec changed.

*If it doesn't work:*

- **`COMPILATION ERROR`:** a Java file has a typo. The error line gives the file and line number. Open it and fix it, or ask Copilot to fix "only the compile error".
- **`Could not resolve dependencies` or `Could not transfer artifact`:** this is a VM problem, not your code. Tell your instructor.
- **Everything is red and you are out of time:** run `bash scripts/catch-up.sh`, then `bash scripts/check-contract.sh`.

---

## You Finished the Core Task

You now have three written assumptions, five Java records generated from a spec, and a test that proves they agree. That is the whole core lab.

---

## Bonus: Keep Going (on your own or if you finish early)

Do these in any order. None are needed for the next lab.

1. **Critique your assumptions (Ask mode).** Attach `assumptions.md` and type: `Which of my three assumptions is riskiest for a bank, and why? Two sentences.`
2. **Architecture trade-offs (Ask mode).** Attach the PRD and `prompts/01-tradeoffs.md`. Do you agree with its recommendation? What would you push back on?
3. **C4 outline and design doc (Ask mode).** Attach the PRD and `prompts/02-c4-and-design-doc.md`. Check the outline: does it show only containers (not classes)? Is every arrow labelled?
4. **Write your own OpenAPI spec (Agent mode).** Attach the PRD, your `assumptions.md` and `prompts/04-openapi.md`. Copilot saves it to `bonus/my-openapi.yaml`, so your working `openapi.yaml` is not touched. Compare the two. What did Copilot do differently? Does it have an `Idempotency-Key` on create?
5. **Vert.x route stubs (Agent mode).** Attach `openapi.yaml` and `prompts/06-route-stubs.md`. Then run `mvn -q compile`. Check that every handler returns **501** (not 200) and that there is no Spring code.
6. **Compare with the reference.** The instructor's reference files are in `../solutions/`. Compare your records with them.

---

## Help: Common Problems and Fixes

| What you see | What it means | What to do |
|---|---|---|
| `command not found: mvn` or `java` | The terminal is not on the lab VM, or the VM image is missing a tool | Make sure you are typing inside the VM, then tell your instructor |
| `No such file or directory: scripts/...` | The terminal is not in the lab folder | Type `cd ~/repo/HSBC-AI-for-SE-Java-4-hours/curriculum/module-1-system-design-and-prd/lab` |
| `openapi.yaml not found` | The terminal or VS Code is in the wrong folder | Same fix as above |
| `class ... not found: generate it with prompts/05-records.md` | A record is missing, or is in the wrong folder or package | Check the file name, the folder, and the first line `package com.ga.hsbc.module1;` |
| `... is missing property ...` | A Java field name differs from the spec | Rename the record component to match `openapi.yaml` exactly |
| Copilot says you have reached your limit | Your monthly Copilot Free allowance has run out | Use `bash scripts/catch-up.sh` and pair with a neighbour for chat steps |
| You made a mess and want to start again | | Type `bash scripts/start-over.sh`, then type `yes`. It resets **this lab folder only** |

---

## Deliverables

- `assumptions.md` with three gaps and your defaults
- Five records in `src/main/java/com/ga/hsbc/module1/`
- `bash scripts/check-contract.sh` reports `PASSED`

## Additional Resources

- Set up GitHub Copilot in VS Code: <https://code.visualstudio.com/docs/copilot/setup>
- OpenAPI Specification 3.0.3: <https://spec.openapis.org/oas/v3.0.3>
- Java records: <https://docs.oracle.com/en/java/javase/21/language/records.html>
- Eclipse Vert.x documentation: <https://vertx.io/docs/>
