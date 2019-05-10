package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Objects

/**
 * A filter expression such as: a = b or a != b
 * Inspired by Hibernate, needs to be fleshed out before it is useful.
 */
class SimpleExpression(
        /** The attribute which shall be filtered for  */
        val attribute: Attribute<*>,
        /** The filter expression, e.g. = or !=  */
        val operation: String,
        /** The value for the RHS of the filter expression  */
        val value: Any?) : Serializable {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as SimpleExpression?
        return attribute == that!!.attribute && operation == that.operation
    }

    override fun hashCode(): Int {
        return Objects.hash(attribute, operation)
    }

    override fun toString(): String {
        return if (value is String) {
            "$attribute$operation'$value'"
        } else attribute.toString() + operation + value

    }

    companion object {

        private const val serialVersionUID = -8984948423945635320L

        /**
         * Creates an equal expression.
         */
        fun <T> eq(propertyName: Attribute<T>, value: T): SimpleExpression {
            return SimpleExpression(propertyName, "=", value)
        }

        /**
         * Creates a not equal expression.
         */
        fun <T> neq(propertyName: Attribute<T>, value: T): SimpleExpression {
            return SimpleExpression(propertyName, "!=", value)
        }
    }
}
