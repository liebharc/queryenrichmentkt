package com.github.liebharc.queryenrichment;

import java.util.Optional;

/**
 * An enrichment produces results by combining values from other attributes and/or other data sources.
 * @param <TAttribute> Attribute type
 */
public abstract class Enrichment<TAttribute, TParameter> implements ExecutableStep<TAttribute, TParameter> {

    private static final long serialVersionUID = -387954492411088733L;

    /** The attribute which is set by this step */
    private final Attribute<TAttribute> attribute;
    /** Optional: Query column, most sof the time this value should be null as most enrichment don't directly query */
    private final String column;
    /** Dependencies of this step */
    private final Dependency dependency;

    public Enrichment(Attribute<TAttribute> attribute, Dependency dependency) {
        this(attribute, null, dependency);
    }

    public Enrichment(Attribute<TAttribute> attribute, String columnOrNull, Dependency dependency) {
        this.attribute = attribute;
        this.column = columnOrNull;
        this.dependency = dependency;
    }

    @Override
    public abstract void enrich(IntermediateResult result, TParameter parameter);

    @Override
    public final Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Dependency getDependencies() {
        return dependency;
    }

    @Override
    public Attribute<TAttribute> getAttribute() {
        return attribute;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return "Enrichment{" +
                "attribute=" + attribute +
                ", column='" + column + '\'' +
                ", dependency=" + dependency +
                '}';
    }
}
