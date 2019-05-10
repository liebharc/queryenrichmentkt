package com.github.liebharc.queryenrichment;

/**
 * A query is planned with a {@link QueryBuilder}. This interface is the result of such a plan and then should only
 * executed the given query by taking the planned query (with placeholders where necessary), setting the values for
 * each placeholder and then running the query.
 */
public interface Query {

    /**
     * Executed the query.
     */
    QueryResult query(Request request);
}
