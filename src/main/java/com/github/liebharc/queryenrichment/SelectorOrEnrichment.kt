package com.github.liebharc.queryenrichment

/**
 * A selector tells a [Query] which properties or columns should be selected.
 * @param <TAttribute> Attrigbute type
</TAttribute> */
open class SelectorOrEnrichment<TAttribute> @JvmOverloads constructor(
        /** The attribute which is set with the given selector  */
        override val attribute: Attribute<TAttribute>,
        /** The column or property name which must be queried for  */
        private val column: String?,
        /** A dependency, normally selectors have no dependencies  */
        override val dependencies: Dependency = Dependencies.noDependencies()) : ExecutableStep<TAttribute, Any?> {

    override fun column(queryInformation: QueryInformation): String? = column

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

        private const val serialVersionUID = -7538684432852388470L
    }
}
