package com.github.liebharc.queryenrichment

import java.util.*

/**
 * Utility class with some default dependencies.
 */
object Dependencies {
    /** Singleton for no dependency  */
    private val noDependency = NoDependency()

    /** A step has no dependencies  */
    fun noDependencies(): Dependency {
        return noDependency
    }

    /** A step requires all of the given attributes  */
    fun requireAll(vararg attributes: Attribute<*>): Dependency {
        return RequireAll(Arrays.asList(*attributes))
    }

    /** A step requires a single attribute  */
    fun require(attribute: Attribute<*>): Dependency {
        return RequireAll(listOf(attribute))
    }

    /** A step requires one of many attributes  */
    fun requireOneOf(vararg attributes: Attribute<*>): Dependency {
        return RequireOneOf(Arrays.asList(*attributes))
    }

    /** A step with only optional dependencies  */
    fun optional(vararg attributes: Attribute<*>): Dependency {
        return OptionalDependency(Arrays.asList(*attributes))
    }

    private class RequireOneOf constructor(private val attributes: Collection<Attribute<*>>) : Dependency {

        override val isEmpty: Boolean
            get() = attributes.isEmpty()

        override fun getMinimalRequiredAttributes(
                selection: Map<Attribute<*>, ExecutableStep<*, *>>,
                available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            if (this.isEmpty) {
                return emptyList()
            }

            val any = attributes.firstOrNull { available.contains(it) }
            return if (any != null) {
                listOf(any)
            } else {
                // We have no match at all, inform the caller about one of the dependencies as this is the minimum
                // we require
                listOf(attributes.iterator().next())
            }
        }

        override fun canBeConstant(attributes: Set<Attribute<*>>): Boolean {
            return if (this.isEmpty) {
                true
            } else {
                this.attributes.any { attributes.contains(it) }
            }

        }
    }


    private class RequireAll constructor(private val attributes: Collection<Attribute<*>>) : Dependency {

        override val isEmpty: Boolean
            get() = attributes.isEmpty()

        override fun getMinimalRequiredAttributes(
                selection: Map<Attribute<*>, ExecutableStep<*, *>>,
                available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            return this.attributes
        }

        override fun canBeConstant(attributes: Set<Attribute<*>>): Boolean {
            return attributes.containsAll(this.attributes)
        }
    }

    private class NoDependency : Dependency {

        override val isEmpty: Boolean
            get() = true

        override fun getMinimalRequiredAttributes(
                selection: Map<Attribute<*>, ExecutableStep<*, *>>,
                available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            return emptyList()
        }

        override fun canBeConstant(attributes: Set<Attribute<*>>): Boolean {
            return true
        }
    }

    private class OptionalDependency constructor(private val attributes: Collection<Attribute<*>>) : Dependency {

        override val isEmpty: Boolean
            get() = attributes.isEmpty()

        override fun getMinimalRequiredAttributes(
                selection: Map<Attribute<*>, ExecutableStep<*, *>>,
                available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            return attributes.filter { atr -> attributes.contains(atr) && selection.containsKey(atr) }
        }

        override fun canBeConstant(attributes: Set<Attribute<*>>): Boolean {
            return false
        }
    }
}
