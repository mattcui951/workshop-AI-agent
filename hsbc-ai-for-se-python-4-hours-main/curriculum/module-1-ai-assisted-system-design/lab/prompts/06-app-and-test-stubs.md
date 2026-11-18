![General Assembly](/curriculum/assets/ga-logo-black-background.png)

# Prompt 6 (bonus): FastAPI stubs and test stubs

From the attached openapi.yaml and models.py, create two files in this lab folder:

1. app.py: a FastAPI app with one route per operationId. Every route returns HTTP 501 with an ErrorResponse body.
2. tests/test_app_stubs.py: pytest tests using fastapi.testclient.TestClient, one per route, asserting the 501 for now.

Do not change models.py, openapi.yaml or tests/test_contract.py. Do not run any terminal commands.
Tell me which of your choices you are least sure about.
