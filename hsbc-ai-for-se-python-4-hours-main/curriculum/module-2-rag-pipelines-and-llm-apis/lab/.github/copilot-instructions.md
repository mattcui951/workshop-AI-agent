# Team conventions for GitHub Copilot

Copilot reads this file automatically when this lab folder is open in VS Code. You do not need to attach it.

- Python 3.12. Type hints on public functions. Standard library first.
- Data models use Pydantic v2. The web layer is FastAPI. No other web frameworks.
- Money is Decimal, never float. Timestamps are timezone-aware datetimes.
- Lightweight on purpose: no vector database, and no LangChain, LlamaIndex, LangGraph or AutoGen.
- Secrets come from environment variables. Never hard-code a credential.
- Tests use pytest. Never edit, delete or weaken anything under tests/.
- Log account numbers masked (last four characters only).
- Change only the files and functions the user asks about. Keep each change small.
- Do not run terminal commands. The learner runs the tests with the scripts in the scripts/ folder.
