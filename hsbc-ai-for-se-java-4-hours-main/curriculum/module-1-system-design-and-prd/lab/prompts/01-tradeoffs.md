# Prompt 1 - architecture trade-offs

I am designing a Java 21 microservice for a precious-metals bullion desk. The requirements are in the attached PRD
(prd/single-cash-flow-prd.md).

Compare two options for the HTTP layer: Vert.x (event loop, small dependency footprint) and Spring Boot.
Give me the three trade-offs that matter most for a team whose biggest recurring pain is patching CVEs,
and recommend one option with reasons.

Then tell me the two things in my PRD you would push back on first.
