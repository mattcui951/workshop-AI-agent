![General Assembly](/curriculum/assets//GA_Horiz_Small_RedBlack_RGB@2x.png)

# Start Here: AI for Software Engineers (Python)

- **Format:** Instructor-Led
- **Duration:** 1 Day • 4 Hours
- **Instructor:** TBD <!-- For delivery, fill in the instructor's name. -->

---

## Overview

This course takes Python engineers from using an AI assistant to write code faster to using it to design, secure and automate engineering work safely. Across four hands-on modules in one session, learners use an assistant to turn a product requirements document into a tested contract, ground an assistant in internal documents with a human review gate, refactor and audit code with tests and a scanner, and build agents that propose changes while a person decides.

Everything runs on a lab VM with synthetic data. No HSBC code or data is used. About 60% of the time is hands-on.

---

## Outcomes

- Use AI to evaluate architecture trade-offs, find gaps in a PRD, and generate OpenAPI specs, Pydantic models and API stubs, then verify them against each other
- Implement chunking, embedding and retrieval in Python, and integrate an LLM API with retrieved context, a relevance guardrail and a human review step
- Refactor Python with AI under test, measure the result, and audit code for vulnerabilities and credential leakage
- Build a function-calling agent and a PR-review agent with layered safety controls and human approval gates

---

## Prerequisites

- Python proficiency, including functions, classes, dataclasses and virtual environments
- Basic familiarity with an AI coding assistant such as GitHub Copilot Chat
- Comfort reading `pytest` output
- No machine learning, security or agent experience is required

---

## System Requirements

The course runs on a provisioned lab VM, so learners install nothing. For reference, the image needs:

- Ubuntu 24.04 LTS Desktop, 4 vCPU, 16 GB RAM, no GPU
- Python 3.12 with `pip` and `venv`, and a prepared environment containing `anthropic`, `pydantic`, `fastapi`, `httpx`, `pytest`, `bandit` and `pyyaml` (plus `openapi-spec-validator`, optional)
- VS Code with the Python and GitHub Copilot extensions, Git, and a web browser
- GitHub Copilot Chat as the learner's assistant
- An `ANTHROPIC_API_KEY` environment variable injected at launch; optionally `CLAUDE_MODEL`
- Network access limited to `api.anthropic.com` at runtime
- Clipboard disabled: prompts are files in each lab's `prompts` folder


---

## 📂 Module Links

Each module and its lab are placed in one folder within this repository with all necessary files and information needed to deliver it.

| Module name | Associated Lab | Tool(s) Used |
|---|---|---|
| [AI-Assisted System Design and PRD Generation](curriculum/module-1-ai-assisted-system-design/ai-assisted-system-design.md) | [PRD-to-Contract Lab](curriculum/module-1-ai-assisted-system-design/ai-assisted-system-design-lab.md) | AI assistant, OpenAPI, Pydantic, FastAPI, pytest |
| [RAG Pipelines and LLM API Integration in Python](curriculum/module-2-rag-pipelines-and-llm-apis/rag-pipelines-and-llm-apis.md) | [RAG Question-Answering Service Lab](curriculum/module-2-rag-pipelines-and-llm-apis/rag-pipelines-and-llm-apis-lab.md) | AI assistant, Claude API, pytest |
| [Performance, Security and Governance in Python](curriculum/module-3-performance-security-and-governance/performance-security-and-governance.md) | [Refactor and Audit Lab](curriculum/module-3-performance-security-and-governance/performance-security-and-governance-lab.md) | AI assistant, pytest, bandit |
| [Agentic Patterns and Workflow Integration](curriculum/module-4-agentic-patterns-and-workflows/agentic-patterns-and-workflows.md) | [Agent and PR-Review Lab](curriculum/module-4-agentic-patterns-and-workflows/agentic-patterns-and-workflows-lab.md) | AI assistant, Claude API, SQLite, pytest |


