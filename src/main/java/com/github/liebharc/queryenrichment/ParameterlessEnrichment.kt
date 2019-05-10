package com.github.liebharc.queryenrichment


/**
 * An [Enrichment] with no paramters.
 * @param <TAttribute> Attribute type.
</TAttribute> */
abstract class ParameterlessEnrichment<TAttribute>(attribute: Attribute<TAttribute>, dependency: Dependency) : Enrichment<TAttribute, Any?>(attribute, dependency) {

    override fun enrich(result: IntermediateResult, parameter: Any?) {
        this.enrich(result)
    }

    /**
     * As [.enrich] without passing a parameter.
     */
    abstract fun enrich(result: IntermediateResult)

    companion object {
        private const val serialVersionUID = -3632157624255773985L
    }
}
