package com.atlas.inventory;

import java.util.List;
import java.util.logging.Logger;

/** Builds the text statement that is sent to a client. */
public final class StatementExporter {
    private static final Logger LOG = Logger.getLogger(StatementExporter.class.getName());

    public String render(String accountNumber, List<Position> positions) {
        LOG.info("Exporting statement for account " + accountNumber);
        StringBuilder sb = new StringBuilder();
        sb.append("Account: ").append(accountNumber).append('\n');
        for (Position p : positions) {
            sb.append(String.format("%s %s %.3f oz%n", p.vaultId(), p.metalCode(), p.quantityOz()));
        }
        return sb.toString();
    }
}
