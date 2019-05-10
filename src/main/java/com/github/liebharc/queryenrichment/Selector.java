package com.github.liebharc.queryenrichment;

import java.util.Optional;

/**
 * A selector tells a {@link Query} which properties or columns should be selected.
 * @param <TAttribute> Attrigbute type
 */
public class Selector<TAttribute> implements ExecutableStep<TAttribute, Object> {

    private static final long serialVersionUID = -7538684432852388470L;
    /** The attribute which is set with the given selector */
    private final Attribute<TAttribute> attribute;
    /** The column or property name which must be queried for */
    private final String column;
    /** A dependency, normally selectors have no dependencies */
    private final Dependency dependency;

    public Selector(Attribute<TAttribute> attribute, String columnOrNull) {
       this(attribute, columnOrNull, Dependencies.noDependencies());
    }

    public Selector(Attribute<TAttribute> attribute, String columnOrNull, Dependency dependency) {
        this.attribute = attribute;
        this.column = columnOrNull;
        this.dependency = dependency;
    }

    @Override
    public Optional<String> getColumn() {
        return Optional.ofNullable(column);
    }

    @Override
    public Attribute<TAttribute> getAttribute() {
        return attribute;
    }

    @Override
    public final void enrich(IntermediateResult result, Object parameter) {
        this.enrich(result);
    }

    public void enrich(IntermediateResult result) {
        result.addFromQuery(this);
    }

    @Override
    public Dependency getDependencies() {
        return dependency;
    }

    @Override
    public final boolean isConstant() {
        return false;
    }

    @Override
    public String toString() {
        return "Step{" +
                "attribute=" + attribute +
                '}';
    }
}
