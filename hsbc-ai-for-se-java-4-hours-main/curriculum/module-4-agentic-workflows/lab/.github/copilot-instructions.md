# Team conventions for GitHub Copilot

Copilot reads this file automatically when this lab folder is open in VS Code. You do not need to attach it.

- Java 21. Use records for data objects. No Lombok.
- The HTTP layer is Vert.x 4.5. Never generate Spring annotations or Spring classes.
- Money and quantities are BigDecimal, never double (except where an existing class already uses double).
- Never block the Vert.x event loop: wrap blocking calls in executeBlocking.
- Secrets come from environment variables. Never hard-code a credential.
- Tests use JUnit 5 and Mockito. Never edit, delete or weaken anything under src/test.
- Log account numbers masked (last four characters only).
- Change only the files and methods the user asks about. Keep each change small.
- Do not run terminal commands. The learner runs the tests with the scripts in the scripts/ folder.
