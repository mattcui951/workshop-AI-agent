package com.ga.hsbc.module1;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Contract check: do YOUR records and YOUR openapi.yaml agree with each other?
 *
 * It looks your types up BY NAME, so it compiles from the start and fails with a clear message until you have created them.
 * Run it from the lab folder:  mvn -q test -Dtest=OpenApiContractTest
 * (A text check: it needs no YAML library.)
 */
class OpenApiContractTest {
    /** Read fresh in every test, so each test fails on its own (with a clear message) until openapi.yaml exists. */
    private static String yaml() {
        Path spec = Path.of("openapi.yaml");
        assertTrue(Files.exists(spec), "openapi.yaml not found in the lab folder: open VS Code and the terminal in the lab folder");
        try {
            return Files.readString(spec);
        } catch (IOException e) {
            return fail("could not read openapi.yaml: " + e.getMessage());
        }
    }

    private static Class<?> type(String simpleName) {
        try {
            return Class.forName("com.ga.hsbc.module1." + simpleName);
        } catch (ClassNotFoundException e) {
            return fail("class com.ga.hsbc.module1." + simpleName + " not found: generate it with prompts/05-records.md");
        }
    }

    /** The text of one schema under components.schemas (4-space indent for the name, 8 for its properties). */
    private static String schemaBlock(String name) {
        Matcher m = Pattern.compile("(?m)^    " + name + ":\\n((?:        .*\\n|      .*\\n|\\s*\\n)*)").matcher(yaml() + "\n");
        assertTrue(m.find(), "schema not found in openapi.yaml: " + name);
        return m.group(1);
    }

    private static void assertRecordMatchesSchema(String recordName, String schema) {
        Class<?> record = type(recordName);
        assertTrue(record.isRecord(), recordName + " should be a Java record");
        String block = schemaBlock(schema);
        for (RecordComponent c : record.getRecordComponents()) {
            assertTrue(block.contains("        " + c.getName() + ":"), schema + " is missing property " + c.getName());
        }
    }

    @Test void requestDtoMatchesTheSpec() { assertRecordMatchesSchema("CreateCashFlowTradeRequest", "CreateCashFlowTradeRequest"); }

    @Test void responseDtoMatchesTheSpec() { assertRecordMatchesSchema("CashFlowTradeResponse", "CashFlowTradeResponse"); }

    @Test void errorDtoMatchesTheSpec() { assertRecordMatchesSchema("ErrorResponse", "ErrorResponse"); }

    @Test void enumsMatchTheSpec() {
        String request = schemaBlock("CreateCashFlowTradeRequest");
        for (Object metal : type("Metal").getEnumConstants()) {
            assertTrue(request.contains(((Enum<?>) metal).name()), "spec is missing metal " + metal);
        }
        assertEquals(4, type("Metal").getEnumConstants().length, "Metal should have GOLD, SILVER, PLATINUM and PALLADIUM");
        String response = schemaBlock("CashFlowTradeResponse");
        for (Object status : type("TradeStatus").getEnumConstants()) {
            assertTrue(response.contains(((Enum<?>) status).name()), "spec is missing status " + status);
        }
        assertEquals(4, type("TradeStatus").getEnumConstants().length, "TradeStatus should have NEW, CONFIRMED, SETTLED and CANCELLED");
    }

    @Test void everyPrdRequirementHasAnEndpoint() {
        assertTrue(yaml().contains("operationId: createTrade"));    // FR1
        assertTrue(yaml().contains("operationId: getTrade"));       // FR5, by id
        assertTrue(yaml().contains("operationId: searchTrades"));   // FR5, by client or date
        assertTrue(yaml().contains("operationId: cancelTrade"));    // FR4
        assertTrue(yaml().contains("Idempotency-Key"));             // FR8
    }
}
