package com.github.liebharc.queryenrichment;

import java.util.Collection;
import java.util.Set;

/**
 * Represents the dependencies of a step.
 */
public interface Dependency {
    /**
     * Given a list of attributes which are already available the dependency has to return the minimal set
     * of attributes it requires so that the step can be executed.
     * @param available List of available attributes
     * @return List of required attributes, this can be but don't have to be in the available list. If an attribute
     * is returned which hasn't been made available yet then the framework will try to add the required steps.
     */
    Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Attribute<?>> available);

    /**
     * Indicates whether or not there are any dependencies.
     */
    boolean isEmpty();

    /**
     * Indicates whether or not the given list of attributes fulfills the dependency.
     */
    boolean isOkay(Set<Attribute<?>> attributes);
}
