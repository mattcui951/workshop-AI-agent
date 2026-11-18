# Example: teaching the assistant your team's conventions

**Reference only. You do not need to copy this anywhere.** A real version of this file is already installed for you at
`.github/copilot-instructions.md` in every lab folder, and Copilot reads it automatically when that lab folder is open in VS Code.
In your own team you would create `.github/copilot-instructions.md` at the root of your repository (check that your organisation allows it).
Short, specific rules beat long essays. This is the kind of file that helps an assistant behave in a codebase with its own habits:

- Java 21. Prefer records for DTOs. No Lombok.
- HTTP layer is Vert.x 4. Do not generate Spring annotations.
- Money and quantities are BigDecimal, never double.
- Never block the Vert.x event loop; wrap blocking calls in executeBlocking.
- Secrets come from environment variables. Never hard-code a credential.
- Tests use JUnit 5 and Mockito. Every bug fix comes with a failing test first.
- Log account numbers masked (last four characters only).
