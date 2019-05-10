package com.github.liebharc.queryenrichment;

import java.io.Serializable;

/**
 * A filter which is implemented by a {@link Query}. That is filters could also be executed during a {@link FilterStep}.
 */
public class QueryFilter {
    /** A filter expression */
    private final SimpleExpression filter;
    /** The query column for the filter */
    private final String column;

    public QueryFilter(SimpleExpression filter) {
        this(filter, filter.getAttribute().getProperty());
    }

    public QueryFilter(SimpleExpression filter, String column) {
        this.filter = filter;
        this.column = column;
    }

    public SimpleExpression getFilter() {
        return filter;
    }

    public String getColumn() {
        return column;
    }

    /**
     * Creates a SQL query expression with a placeholder value (e.g. "ID = ?").
     */
    public String toPlaceHolderString() {
        return column + filter.getOperation() + "?";
    }
}
