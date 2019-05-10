package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.Objects;

/**
 * A filter expression such as: a = b or a != b
 * Inspired by Hibernate, needs to be fleshed out before it is useful.
 */
public class SimpleExpression implements Serializable {

    private static final long serialVersionUID = -8984948423945635320L;

    /**
     * Creates an equal expression.
     */
    public static<T> SimpleExpression eq(Attribute<T> propertyName, T value) {
        return new SimpleExpression(propertyName, "=", value);
    }

    /**
     * Creates a not equal expression.
     */
    public static<T> SimpleExpression neq(Attribute<T>  propertyName, T value) {
        return new SimpleExpression(propertyName, "!=", value);
    }

    /** The attribute which shall be filtered for */
    private final Attribute<?> attribute;
    /** The filter expression, e.g. = or != */
    private final String operation;
    /** The value for the RHS of the filter expression */
    private final Object value;

    public SimpleExpression(Attribute<?> attribute, String operation, Object value) {
        this.attribute = attribute;
        this.operation = operation;
        this.value = value;
    }

    public Attribute<?> getAttribute() {
        return attribute;
    }

    public String getOperation() {
        return operation;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleExpression that = (SimpleExpression) o;
        return Objects.equals(attribute, that.attribute) &&
                Objects.equals(operation, that.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attribute, operation);
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return attribute + operation + "'" + value + "'";
        }

        return attribute + operation + value;
    }
}
