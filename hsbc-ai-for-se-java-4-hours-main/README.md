![General Assembly](/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Start Here: AI for Software Engineers (Java)

- **Format:** Instructor-Led
- **Duration:** 1 Day • 4 Hours
- **Instructor:** TBD <!-- For delivery, fill in the instructor's name. -->

---

## Overview

This course takes Java engineers from using an AI assistant to write code faster to using it to design, secure and automate engineering work safely. Across four hands-on modules in one session, learners use an assistant to turn a product requirements document into a tested contract, ground an assistant in internal documents with a human review gate, refactor and audit code (including vulnerable dependencies) with tests and scanners, and build agents that propose changes while a person decides.

Everything runs on a lab VM with synthetic data. No HSBC code or data is used. About half of the time is independent lab work, and the guided builds are hands-on too.

---

## Outcomes

- Use AI to evaluate architecture trade-offs, find gaps in a PRD, and generate OpenAPI specs, Java records and Vert.x route stubs, then verify them against each other
- Implement chunking, embedding and retrieval in Java, and integrate an LLM API with retrieved context, a relevance guardrail, a human review step and a Vert.x endpoint
- Refactor Java with AI under test, measure the result, and audit code for vulnerabilities, credential leakage and vulnerable dependencies
- Build a function-calling agent and a PR-review agent with layered safety controls and human approval gates

---

## Prerequisites

- Java proficiency, including records, interfaces and JUnit
- Basic familiarity with Maven and an AI coding assistant such as GitHub Copilot Chat
- Comfort reading test output
- No machine learning, security or agent experience is required

---

## System Requirements

The course runs on a provisioned lab VM, so learners install nothing. For reference, the image needs:

- Ubuntu 24.04 LTS Desktop, 4 vCPU, 16 GB RAM, no GPU
- JDK 21 and Maven 3.9, with a **pre-populated local Maven repository** (the VM cannot reach Maven Central)
- VS Code with the Java extension pack, or IntelliJ IDEA, with the GitHub Copilot extension or plugin
- GitHub Copilot Chat as the learner's assistant
- An `ANTHROPIC_API_KEY` environment variable injected at launch; optionally `CLAUDE_MODEL`
- Network access limited to `api.anthropic.com` at runtime; lab servers use `localhost`
- Clipboard disabled: prompts are files in each lab's `prompts` folder


---

## 📂 Module Links

Each module and its lab are placed in one folder within this repository with all necessary files and information needed to deliver it.

| Module name | Associated Lab | Tool(s) Used |
|---|---|---|
| [System Design and PRD](curriculum/module-1-system-design-and-prd/system-design-and-prd.md) | [PRD-to-Contract Lab](curriculum/module-1-system-design-and-prd/system-design-and-prd-lab.md) | AI assistant, OpenAPI, Java records, Vert.x, JUnit |
| [Java RAG Pipelines](curriculum/module-2-java-rag-pipelines/java-rag-pipelines.md) | [Java RAG Question-Answering Service Lab](curriculum/module-2-java-rag-pipelines/java-rag-pipelines-lab.md) | AI assistant, Claude API, Vert.x, JUnit |
| [Performance and Security](curriculum/module-3-performance-and-security/performance-and-security.md) | [Refactor and Audit Lab](curriculum/module-3-performance-and-security/performance-and-security-lab.md) | AI assistant, JUnit, Mockito, code and dependency scanners |
| [Agentic Workflows](curriculum/module-4-agentic-workflows/agentic-workflows.md) | [Agent and PR-Review Lab](curriculum/module-4-agentic-workflows/agentic-workflows-lab.md) | AI assistant, Claude API, H2, JUnit |


