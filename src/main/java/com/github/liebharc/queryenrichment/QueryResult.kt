package com.github.liebharc.queryenrichment

/**
 * The result of [Query].
 */
data class QueryResult(
        /**
         * Resulting rows.
         */
        val rows: List<List<Any?>>)
