package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.List;

/**
 * The result of {@link Plan#execute(Request)}.
 */
public class EnrichedQueryResult implements Serializable {

    private static final long serialVersionUID = -5772452905687791428L;

    /** List of queried attributes */
    private final List<Attribute<?>> attributes;
    /** List of results */
    private final Object[][] results;

    public EnrichedQueryResult(List<Attribute<?>> attributes, Object[][] results) {
        this.attributes = attributes;
        this.results = results;
    }

    public List<Attribute<?>> getAttributes() {
        return attributes;
    }

    public Object[][] getResults() {
        return results;
    }
}
