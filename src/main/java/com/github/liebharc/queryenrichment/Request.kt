package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Objects

/**
 * A query request.
 */
class Request @JvmOverloads constructor(
        /** Attributes which should be selected  */
        val attributes: List<Attribute<*>>,
        /** Filter conditions  */
        val criteria: List<SimpleExpression> = emptyList()) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val request = other as Request?
        return attributes == request!!.attributes && criteria == request.criteria
    }

    override fun hashCode(): Int {
        return Objects.hash(attributes, criteria)
    }

    companion object {
        private const val serialVersionUID = -3481567358246544063L
    }
}
