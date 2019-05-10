package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A query request.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = -3481567358246544063L;

    /** Attributes which should be selected */
    private final List<Attribute<?>> attributes;

    /** Filter conditions */
    private final List<SimpleExpression> criteria;

    public Request(List<Attribute<?>> attributes) {
        this(attributes, Collections.emptyList());
    }

    public Request(List<Attribute<?>> attributes, List<SimpleExpression> criteria) {
        this.attributes = attributes;
        this.criteria = criteria;
    }

    public List<Attribute<?>> getAttributes() {
        return attributes;
    }

    public List<SimpleExpression> getCriteria() {
        return criteria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Request request = (Request) o;
        return Objects.equals(attributes, request.attributes) &&
                Objects.equals(criteria, request.criteria);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes, criteria);
    }
}
