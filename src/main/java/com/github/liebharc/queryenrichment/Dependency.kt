package com.github.liebharc.queryenrichment

/**
 * Represents the dependencies of a step.
 */
interface Dependency {

    /**
     * Indicates whether or not there are any dependencies.
     */
    val isEmpty: Boolean

    /**
     * Given a list of attributes which are already available the dependency has to return the minimal set
     * of attributes it requires so that the step can be executed.
     * @param available List of available attributes
     * @return List of required attributes, this can be but don't have to be in the available list. If an attribute
     * is returned which hasn't been made available yet then the framework will try to add the required steps.
     */
    fun getMinimalRequiredAttributes(available: Collection<Attribute<*>>): Collection<Attribute<*>>

    /**
     * Indicates whether or not the given list of attributes fulfills the dependency.
     */
    fun isOkay(attributes: Set<Attribute<*>>): Boolean
}
