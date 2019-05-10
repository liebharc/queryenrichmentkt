package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * A query request.
 */
data class Request constructor(
        /** Attributes which should be selected  */
        val attributes: List<Attribute<*>>,
        /** Filter conditions  */
        val criteria: List<SimpleExpression<*>> = emptyList()) : Serializable {

    companion object {
        private const val serialVersionUID = -3481567358246544063L
    }
}
