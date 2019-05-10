package com.github.liebharc.queryenrichment

import java.util.Optional

/**
 * A selector tells a [Query] which properties or columns should be selected.
 * @param <TAttribute> Attrigbute type
</TAttribute> */
open class Selector<TAttribute> @JvmOverloads constructor(
        /** The attribute which is set with the given selector  */
        override val attribute: Attribute<TAttribute>,
        /** The column or property name which must be queried for  */
        override val column: String?,
        /** A dependency, normally selectors have no dependencies  */
        override val dependencies: Dependency = Dependencies.noDependencies()) : ExecutableStep<TAttribute, Any?> {

    override val canBeConstant: Boolean
        get() = false

    override fun enrich(result: IntermediateResult, parameter: Any?) {
        this.enrich(result)
    }

    fun enrich(result: IntermediateResult) {
        result.addFromQuery(this)
    }

    override fun toString(): String {
        return "Step{" +
                "attribute=" + attribute +
                '}'.toString()
    }

    companion object {

        private val serialVersionUID = -7538684432852388470L
    }
}
