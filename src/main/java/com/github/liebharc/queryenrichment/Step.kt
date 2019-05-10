package com.github.liebharc.queryenrichment

import java.io.Serializable
import java.util.Optional

/**
 * Definition of the meta data about a step which is executed to get the result for a query.
 * @param <TAttribute> Attribute type
</TAttribute> */
interface Step<TAttribute> : Serializable {

    /** Returns the related column/property name for a query if there is one  */
    val column: String?

    /** Returns the attribute which is set during this step  */
    val attribute: Attribute<TAttribute>

    /**
     * Gets the dependencies of this step.
     */
    val dependencies: Dependency

    /**
     * Indicates whether or not the step is constant. Constant steps are steps which will return the same value for all
     * rows in a query.
     */
    val isConstant: Boolean

    companion object {

        /** Constant which can be passed if a step has no direct relation to a column/property  */
        val NO_COLUMN: String? = null
    }
}
