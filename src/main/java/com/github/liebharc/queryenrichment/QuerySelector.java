package com.github.liebharc.queryenrichment;

/**
 * A selector which is implemented by a {@link Query}.
 */
public class QuerySelector {
    /** A attribute expression */
    private final Attribute<?> attribute;
    /** The query column for the attribute */
    private final String column;

    public QuerySelector(Attribute<?> attribute) {
        this(attribute, attribute.getProperty());
    }

    public QuerySelector(Attribute<?> attribute, String column) {
        this.attribute = attribute;
        this.column = column;
    }

    public Attribute<?> getAttribute() {
        return attribute;
    }

    public String getColumn() {
        return column;
    }
}
