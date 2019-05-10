package com.github.liebharc.queryenrichment;

/**
 * Helper class to make it easier to build selectors.
 * @param <T> Attribute type.
 */
public class SelectorBuilder<T> {
    /** The attribute for the selector */
    private Attribute<T> attribute;
    /** The column or property name */
    private String column;

    public SelectorBuilder(Attribute<T> attribute) {
        this.attribute = attribute;
        this.column = attribute.getProperty();
    }

    public SelectorBuilder<T> addColumn(String column) {
        this.column = column;
        return this;
    }

    public Step<T> build() {
        return new Selector<>(attribute, column);
    }
}
