![General Assembly](/curriculum/assets/ga-logo-black-background.png)

# Prompt 1: architecture trade-offs

I am designing a small Python microservice. The requirements are in the attached PRD (prd/payment-status-prd.md).

Compare two designs for how the service stores and serves payment status:
1. a synchronous REST API backed by a relational database
2. an event-sourced design that keeps every event and derives the status

Give me the three trade-offs that matter most for a bank, and recommend one design with reasons.
Then tell me the two things in my PRD you would push back on first.
