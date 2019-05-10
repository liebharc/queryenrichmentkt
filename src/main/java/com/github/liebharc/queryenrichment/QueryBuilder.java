package com.github.liebharc.queryenrichment;

import java.util.List;

/**
 * A query builder prepares a {@link Query}.
 */
public interface QueryBuilder {
    /**
     * Builds a query.
     * @param selectors Select expression
     * @param filters FilterStep or where expression
     * @return Query object
     */
    Query build(List<QuerySelector> selectors, List<QueryFilter> filters);
}