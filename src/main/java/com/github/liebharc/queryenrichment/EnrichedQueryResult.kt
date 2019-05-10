package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * The result of [Plan.execute].
 */
class EnrichedQueryResult(
        /** List of queried attributes  */
        val attributes: List<Attribute<*>>,
        /** List of results  */
        val results: Array<Array<Any?>>) : Serializable {
    companion object {

        private const val serialVersionUID = -5772452905687791428L
    }
}
