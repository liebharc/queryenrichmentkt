package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * A filter expression such as: a = b or a != b
 * Inspired by Hibernate, needs to be fleshed out before it is useful.
 */
data class SimpleExpression<TAttribute>(
        /** The attribute which shall be filtered for  */
        val attribute: Attribute<*>,
        /** The filter expression, e.g. = or !=  */
        val operation: String,
        /** The value for the RHS of the filter expression  */
        val value: TAttribute) : Serializable {

    companion object {

        private const val serialVersionUID = -8984948423945635320L

        /**
         * Creates an equal expression.
         */
        fun <T> eq(propertyName: Attribute<T>, value: T): SimpleExpression<T> {
            return SimpleExpression(propertyName, "=", value)
        }

        /**
         * Creates a not equal expression.
         */
        fun <T> neq(propertyName: Attribute<T>, value: T): SimpleExpression<T> {
            return SimpleExpression(propertyName, "!=", value)
        }
    }
}
