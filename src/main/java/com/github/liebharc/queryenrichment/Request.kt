package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Collections
import java.util.Objects

/**
 * A query request.
 */
class Request @JvmOverloads constructor(
        /** Attributes which should be selected  */
        val attributes: List<Attribute<*>>,
        /** Filter conditions  */
        val criteria: List<SimpleExpression> = emptyList()) : Serializable {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val request = o as Request?
        return attributes == request!!.attributes && criteria == request.criteria
    }

    override fun hashCode(): Int {
        return Objects.hash(attributes, criteria)
    }

    companion object {
        private const val serialVersionUID = -3481567358246544063L
    }
}
