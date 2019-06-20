package com.github.liebharc.queryenrichment

/**
 * A selector tells a [Query] which properties or columns should be selected.
 * @param <TAttribute> Attrigbute type
</TAttribute> */
open class SelectorOrEnrichment<TAttribute, TReference> constructor(
        /** The attribute which is set with the given selector  */
        override val attribute: Attribute<TAttribute>,
        /** The column or property name which must be queried for  */
        private val column: String?,
        /** A reference which will be used if available */
        private val reference: Attribute<TReference>,
        /** Getter */
        private val accessor: ((TReference) -> (TAttribute))) : ExecutableStep<TAttribute, Any?> {

    override val dependencies: Dependency = Dependencies.optional(reference)

    override fun column(queryInformation: QueryInformation): String? =
            if (queryInformation.hasReference) { null } else { column }

    override fun enrich(result: IntermediateResult, parameter: Any?) {
        this.enrich(result)
    }

    fun enrich(result: IntermediateResult) {
        val fromReference = result.get(reference)
        if (fromReference != null) {
            result.add(this, accessor(fromReference))
        }
        else {
            result.addFromQuery(this)
        }
    }

    override fun toString(): String {
        return "SelectorOrEnrichment{" +
                "attribute=" + attribute +
                '}'.toString()
    }

    companion object {

        private const val serialVersionUID = -7538684432852388470L
    }
}
