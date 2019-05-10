package com.github.liebharc.queryenrichment


/**
 * An [Enrichment] with no paramters.
 * @param <TAttribute> Attribute type.
</TAttribute> */
abstract class ParameterlessEnrichment<TAttribute> : Enrichment<TAttribute, Any?> {

    constructor(attribute: Attribute<TAttribute>, dependency: Dependency) : super(attribute, dependency) {}

    constructor(attribute: Attribute<TAttribute>, columnOrNull: String, dependency: Dependency) : super(attribute, columnOrNull, dependency) {}

    override fun enrich(result: IntermediateResult, parameter: Any?) {
        this.enrich(result)
    }

    /**
     * As [.enrich] without passing a parameter.
     */
    abstract fun enrich(result: IntermediateResult)

    companion object {
        private val serialVersionUID = -3632157624255773985L
    }
}
