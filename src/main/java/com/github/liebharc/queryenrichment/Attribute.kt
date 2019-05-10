package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Objects

/**
 * An attribute which can be queried.
 * @param <T> Attribute type
</T> */
class Attribute<T>(
        /** Class of the attribute type  */
        val attributeClass: Class<T>,
        /** The domain ob the attribute, could be a database table or an object type  */
        val domain: String,
        /** The property of the attribute, could be a database column or an object property  */
        val property: String) : Serializable {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val attribute = o as Attribute<*>?
        return domain == attribute!!.domain && property == attribute.property
    }

    override fun hashCode(): Int {
        return Objects.hash(domain, property)
    }

    override fun toString(): String {
        return "Attribute{" +
                "domain='" + domain + '\''.toString() +
                ", property='" + property + '\''.toString() +
                '}'.toString()
    }

    companion object {

        private const val serialVersionUID = -3323488022561687505L
    }
}
