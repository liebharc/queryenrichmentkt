package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * A filter which is implemented by a [Query]. That is filters could also be executed during a [FilterStep].
 */
class QueryFilter @JvmOverloads constructor(
        /** A filter expression  */
        val filter: SimpleExpression<*>,
        /** The query column for the filter  */
        val column: String = filter.attribute.property) {

    /**
     * Creates a SQL query expression with a placeholder value (e.g. "ID = ?").
     */
    fun toPlaceHolderString(): String {
        return column + filter.operation + "?"
    }
}
