package com.github.liebharc.queryenrichment;


/**
 * An {@link Enrichment} with no paramters.
 * @param <TAttribute> Attribute type.
 */
public abstract class ParameterlessEnrichment<TAttribute> extends Enrichment<TAttribute, Object> {
    private static final long serialVersionUID = -3632157624255773985L;

    public ParameterlessEnrichment(Attribute<TAttribute> attribute, Dependency dependency) {
        super(attribute, dependency);
    }

    public ParameterlessEnrichment(Attribute<TAttribute> attribute, String columnOrNull, Dependency dependency) {
        super(attribute, columnOrNull, dependency);
    }

    @Override
    public final void enrich(IntermediateResult result, Object parameter) {
        this.enrich(result);
    }

    /**
     * As {@link #enrich(IntermediateResult, Object)} without passing a parameter.
     */
    public abstract void enrich(IntermediateResult result);
}
