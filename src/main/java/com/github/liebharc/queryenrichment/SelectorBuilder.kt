package com.github.liebharc.queryenrichment

/**
 * Helper class to make it easier to build selectors.
 * @param <T> Attribute type.
</T> */
class SelectorBuilder<T>(
        /** The attribute for the selector  */
        private val attribute: Attribute<T>) {
    /** The column or property name  */
    private var column: String? = null

    init {
        this.column = attribute.property
    }

    fun addColumn(column: String): SelectorBuilder<T> {
        this.column = column
        return this
    }

    fun build(): ExecutableStep<T, Any?> {
        return Selector(attribute, column)
    }
}
