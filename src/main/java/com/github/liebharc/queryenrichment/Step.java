package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.Optional;

/**
 * Definition of the meta data about a step which is executed to get the result for a query.
 * @param <TAttribute> Attribute type
 */
public interface Step<TAttribute> extends Serializable {

    /** Constant which can be passed if a step has no direct relation to a column/property */
    String NO_COLUMN = null;

    /** Returns the related column/property name for a query if there is one */
    Optional<String> getColumn();

    /** Returns the attribute which is set during this step */
    Attribute<TAttribute> getAttribute();

    /**
     * Gets the dependencies of this step.
     */
    Dependency getDependencies();

    /**
     * Indicates whether or not the step is constant. Constant steps are steps which will return the same value for all
     * rows in a query.
     */
    boolean isConstant();
}
