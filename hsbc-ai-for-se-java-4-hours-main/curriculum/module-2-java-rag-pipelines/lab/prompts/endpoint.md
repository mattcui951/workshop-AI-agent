# Prompt - Vert.x RAG endpoint with a human review gate

Create the file src/main/java/com/ga/hsbc/module2/web/RagServer.java: a Vert.x 4.5 verticle class RagServer in package com.ga.hsbc.module2.web that exposes the existing RagService
(attached) and ReviewQueue (attached) as a REST API:

- POST /ask {"question": "..."}  -> 202 {"id","status":"PENDING_REVIEW"} when the answer is awaiting review,
                                    200 {"status":"DECLINED",...} when no relevant source was found, 400 if the question is missing
- GET  /answers/:id              -> 200 with the answer ONLY if a reviewer approved it; 403 if pending or rejected; 404 if unknown
- GET  /reviews                  -> the pending drafts, for the reviewer
- POST /reviews/:id/approve  {"reviewer": "..."}  and  POST /reviews/:id/reject {"reviewer": "...", "reason": "..."}

Rules:
- RagService.ask() makes a blocking HTTP call. It must NOT run on the Vert.x event loop. Use executeBlocking.
- Do not put the answer text in any response until it is approved.
- Return JSON error bodies. Tell me where you are least sure about the Vert.x API.
- Include a main method that builds the index with RagApp.buildIndex(Path.of("sample_docs")), creates the service with
  RagApp.service(index, ClaudeClient.fromEnv()), and deploys the verticle on port 8080.
- Create the file. Do not run any terminal commands.
