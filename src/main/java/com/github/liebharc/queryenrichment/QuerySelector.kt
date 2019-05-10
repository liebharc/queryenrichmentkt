package com.github.liebharc.queryenrichment

/**
 * A selector which is implemented by a [Query].
 */
data class QuerySelector @JvmOverloads constructor(
        /** A attribute expression  */
        val attribute: Attribute<*>,
        /** The query column for the attribute  */
        val column: String = attribute.property)
