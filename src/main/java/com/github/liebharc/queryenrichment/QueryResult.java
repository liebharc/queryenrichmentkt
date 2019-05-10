package com.github.liebharc.queryenrichment;

import java.util.List;

/**
 * The result of {@link Query}.
 */
public class QueryResult {
    /**
     * Resulting rows.
     */
    private final List<List<Object>> rows;

    public QueryResult(List<List<Object>> rows) {
        this.rows = rows;
    }

    public List<List<Object>> getRows() {
        return rows;
    }
}
