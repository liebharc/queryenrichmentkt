package com.github.liebharc.queryenrichment

import java.util.*
import java.util.function.Predicate

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

    private class RequireOneOf constructor(private val attributes: Collection<Attribute<*>>) : Dependency {

        override val isEmpty: Boolean
            get() = attributes.isEmpty()

        override fun getMinimalRequiredAttributes(available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            if (this.isEmpty) {
                return emptyList()
            }

            val any = attributes.stream().filter(Predicate<Attribute<*>> { available.contains(it) }).findAny()
            return if (any.isPresent) {
                listOf(any.get())
            } else {
                // We have no match at all, inform the caller about one of the dependencies as this is the minimum
                // we require
                listOf(attributes.iterator().next())
            }
        }

        override fun isOkay(available: Set<Attribute<*>>): Boolean {
            return if (this.isEmpty) {
                true
            } else this.attributes.stream().anyMatch(Predicate<Attribute<*>> { available.contains(it) })

        }
    }


    private class RequireAll constructor(private val attributes: Collection<Attribute<*>>) : Dependency {

        override val isEmpty: Boolean
            get() = attributes.isEmpty()

        override fun getMinimalRequiredAttributes(available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            return this.attributes
        }

        override fun isOkay(available: Set<Attribute<*>>): Boolean {
            return available.containsAll(this.attributes)
        }
    }

    private class NoDependency : Dependency {

        override val isEmpty: Boolean
            get() = true

        override fun getMinimalRequiredAttributes(available: Collection<Attribute<*>>): Collection<Attribute<*>> {
            return emptyList()
        }

        override fun isOkay(constantAttributes: Set<Attribute<*>>): Boolean {
            return true
        }
    }
}// Utility class
