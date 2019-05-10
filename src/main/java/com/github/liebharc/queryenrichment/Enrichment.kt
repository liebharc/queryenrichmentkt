package com.github.liebharc.queryenrichment

import java.util.Optional

/**
 * An enrichment produces results by combining values from other attributes and/or other data sources.
 * @param <TAttribute> Attribute type
</TAttribute> */
abstract class Enrichment<TAttribute, TParameter>(
        /** The attribute which is set by this step  */
        override val attribute: Attribute<TAttribute>,
        /** Optional: Query column, most sof the time this value should be null as most enrichment don't directly query  */
        override val column: String?,
        /** Dependencies of this step  */
        override val dependencies: Dependency) : ExecutableStep<TAttribute, TParameter> {

    override val canBeConstant: Boolean
        get() = true

    constructor(attribute: Attribute<TAttribute>, dependency: Dependency) : this(attribute, null, dependency) {}

    abstract override fun enrich(result: IntermediateResult, parameter: TParameter)

    override fun toString(): String {
        return "Enrichment{" +
                "attribute=" + attribute +
                ", column='" + column + '\''.toString() +
                ", dependency=" + dependencies +
                '}'.toString()
    }

    companion object {

        private val serialVersionUID = -387954492411088733L
    }
}
