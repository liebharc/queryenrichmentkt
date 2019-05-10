package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * The result of [Plan.execute].
 */
data class EnrichedQueryResult(
        /** List of queried attributes  */
        val attributes: List<Attribute<*>>,
        /** List of results  */
        val results: Array<Array<Any?>>) : Serializable {
    companion object {

        private const val serialVersionUID = -5772452905687791428L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EnrichedQueryResult

        if (attributes != other.attributes) return false
        if (!results.contentDeepEquals(other.results)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = attributes.hashCode()
        result = 31 * result + results.contentDeepHashCode()
        return result
    }
}
