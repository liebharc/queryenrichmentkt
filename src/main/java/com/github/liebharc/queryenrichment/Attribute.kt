package com.github.liebharc.queryenrichment

import java.io.Serializable

/**
 * An attribute which can be queried.
 * @param <T> Attribute type
</T> */
data class Attribute<T>(
        /** Class of the attribute type  */
        val attributeClass: Class<T>,
        /** The domain ob the attribute, could be a database table or an object type  */
        val domain: String,
        /** The property of the attribute, could be a database column or an object property  */
        val property: String) : Serializable {

    companion object {

        private const val serialVersionUID = -3323488022561687505L

        val reference: String = "ref"
    }
}
