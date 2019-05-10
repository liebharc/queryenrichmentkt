package com.github.liebharc.queryenrichment

/**
 * A query builder prepares a [Query].
 */
interface QueryBuilder {
    /**
     * Builds a query.
     * @param selectors Select expression
     * @param filters FilterStep or where expression
     * @return Query object
     */
    fun build(selectors: List<QuerySelector>, filters: List<QueryFilter>): Query
}