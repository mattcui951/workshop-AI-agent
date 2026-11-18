package com.ga.hsbc.module2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MiniJsonTest {
    @Test void parsesNestedStructures() {
        Map<String, Object> m = MiniJson.obj(MiniJson.parse(
                "{\"a\":1,\"b\":[true,false,null,2.5],\"c\":{\"d\":\"x\"}}"));
        assertEquals(1L, m.get("a"));
        assertEquals(List.of(true, false, 2.5).size() + 1, MiniJson.arr(m.get("b")).size());
        assertEquals("x", MiniJson.obj(m.get("c")).get("d"));
    }

    @Test void roundTripsEscapesAndUnicode() {
        String original = "line1\nline2\t\"quoted\" back\\slash caf\u00e9 \u2713";
        String json = MiniJson.toJson(Map.of("t", original));
        assertEquals(original, MiniJson.obj(MiniJson.parse(json)).get("t"));
    }

    @Test void parsesUnicodeEscapes() {
        assertEquals("A\u00e9", MiniJson.parse("\"A\u00e9\""));
    }

    @Test void rejectsMalformedInput() {
        assertThrows(IllegalArgumentException.class, () -> MiniJson.parse("{\"a\":}"));
        assertThrows(IllegalArgumentException.class, () -> MiniJson.parse("[1,2"));
        assertThrows(IllegalArgumentException.class, () -> MiniJson.parse("{} extra"));
    }
}
