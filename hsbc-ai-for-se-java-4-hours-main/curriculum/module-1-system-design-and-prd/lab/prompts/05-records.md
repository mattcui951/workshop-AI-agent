# Prompt 5 - Java records from the OpenAPI spec

From the attached openapi.yaml, create these five files in src/main/java/com/ga/hsbc/module1/ (package com.ga.hsbc.module1):
Metal.java (enum), TradeStatus.java (enum), CreateCashFlowTradeRequest.java, CashFlowTradeResponse.java and ErrorResponse.java.
Use exactly these names.

- The three DTOs are Java 21 records. Each record component has exactly the same name as the property in the spec.
- Use BigDecimal for quantities, prices and money. Never double. Use LocalDate for dates.
- Validate in the record constructor: required fields not null or blank, positive amounts, 3-letter upper-case currency.
- No Spring, no Lombok, no other libraries.

Create the files. Do not run any terminal commands. Then tell me, in two lines, which choice you are least sure about.
