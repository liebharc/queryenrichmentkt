package com.github.liebharc.queryenrichment;

/**
 * A shortcut for a select where the column name matches the property name of the attribute.
 * @param <T> Attribute type
 */
public class PropertySelector<T> extends Selector<T> {
    private static final long serialVersionUID = 4121898114652929366L;

    public PropertySelector(Attribute<T> attribute, Dependency dependency) {
        super(attribute, attribute.getProperty(), dependency);
    }
}
