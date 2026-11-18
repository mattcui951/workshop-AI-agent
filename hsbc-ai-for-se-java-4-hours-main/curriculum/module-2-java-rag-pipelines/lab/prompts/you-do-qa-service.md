# Prompt: write the prompt builder and the service

I am building the question-answering half of a small retrieval system in Java 21. The specification is in the Javadoc of the
attached PromptBuilder.java and RagService.java, and the tests are in the attached PromptBuilderTest.java and RagServiceTest.java.
The other classes (Chunker, Embedder, InMemoryIndex, ReviewQueue) are already finished: do not change them.

Write PromptBuilder.build() first, then RagService.ask(). Follow the Javadoc exactly. Change only those two methods.
Do not run any terminal commands: I will run the tests myself.

When you are done, tell me in one or two sentences each:
- how build() stops a document from closing its own <source> tag
- one way a malicious document could still try to influence the model even after the escaping
