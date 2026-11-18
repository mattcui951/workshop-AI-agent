package com.ga.hsbc.module4;

import java.util.Map;
import java.util.Scanner;

/** Human-in-the-loop: nothing runs until a person says yes. */
public interface ApprovalGate {
    boolean approve(String action, Map<String, Object> details);

    /** Asks on the console. Set AUTO_APPROVE=1 only for unattended test runs - never for real use. */
    static ApprovalGate fromEnvironment() {
        if ("1".equals(System.getenv("AUTO_APPROVE"))) {
            return (a, d) -> { System.out.println("[approval] AUTO_APPROVE set - approving: " + a); return true; };
        }
        Scanner in = new Scanner(System.in);
        return (action, details) -> {
            System.out.println("\n[approval gate] The agent wants to: " + action + " " + MiniJson.toJson(details));
            System.out.print("Approve? (y/n): ");
            return in.hasNextLine() && in.nextLine().strip().equalsIgnoreCase("y");
        };
    }
}
