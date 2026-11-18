# Prompt: write the prompt builder and the service

I am building the question-answering half of a small retrieval system in Python. The specification is in the docstrings of
build_prompt and RagService.ask in the attached qa_service.py, and the tests are in the attached tests/test_qa_service.py.
ingest.py and the ReviewQueue at the bottom of qa_service.py are already finished: do not change them.

Write build_prompt() first, then RagService.ask(). Follow the docstrings exactly. Change only those two functions.
Do not run any terminal commands: I will run the tests myself.

When you are done, tell me in one or two sentences each:
- how build_prompt() stops a document from closing its own <source> tag
- one way a malicious document could still try to influence the model even after the escaping
