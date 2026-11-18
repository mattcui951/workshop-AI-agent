// PRE-BUILT FOR YOU: this file is finished. You do not need to change it. Read it if you are curious.
package com.ga.hsbc.module4;

import java.util.regex.Pattern;

/**
 * First line of defence for the SQL tool: accept exactly one plain SELECT statement.
 *
 * This is a backstop, not the real control. String checks can be bypassed in ways nobody has thought of yet;
 * the real control is the read-only database account (see Database). Keep both.
 * The check is deliberately conservative: it may reject a harmless query that merely contains a forbidden word.
 */
public final class SafeSql {
    public static final int MAX_LENGTH = 2000;

    private static final Pattern STARTS_WITH_SELECT = Pattern.compile("^select\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|create|truncate|merge|call|grant|revoke|exec|execute|into|shutdown|script|runscript|information_schema)\\b"
                    + "|\\bfor\\s+update\\b", Pattern.CASE_INSENSITIVE);

    private SafeSql() {}

    /** Returns the query to run (trailing semicolon removed) or throws IllegalArgumentException. */
    public static String validate(String sql) {
        if (sql == null || sql.isBlank()) throw new IllegalArgumentException("SQL is empty");
        String s = sql.strip();
        if (s.endsWith(";")) s = s.substring(0, s.length() - 1).stripTrailing();
        if (s.length() > MAX_LENGTH) throw new IllegalArgumentException("SQL is too long");
        if (!STARTS_WITH_SELECT.matcher(s).find()) throw new IllegalArgumentException("Only SELECT statements are allowed");
        if (s.contains(";")) throw new IllegalArgumentException("Only one statement is allowed");
        if (s.contains("--") || s.contains("/*")) throw new IllegalArgumentException("SQL comments are not allowed");
        if (FORBIDDEN.matcher(s).find()) throw new IllegalArgumentException("Query contains a forbidden keyword");
        return s;
    }
}
