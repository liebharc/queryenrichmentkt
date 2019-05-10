package com.github.liebharc.queryenrichment

/**
 * A shortcut for a select where the column name matches the property name of the attribute.
 * @param <T> Attribute type
</T> */
class PropertySelector<T>(attribute: Attribute<T>, dependency: Dependency) : Selector<T>(attribute, attribute.property, dependency) {
    companion object {
        private val serialVersionUID = 4121898114652929366L
    }
}
