package com.github.liebharc.queryenrichment;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A helper to draw a simple documentation for the attribute schema
 */
public class SchemaDocumentation {

    /** Singleton */
    public static final SchemaDocumentation INSTANCE = new SchemaDocumentation();

    private SchemaDocumentation() {
        // Singleton class
    }

    public String drawSchema(Collection<Attribute<?>> attributes) {
        final StringBuilder result = new StringBuilder();
        final Map<String, List<Attribute>> byDomain =
                attributes.stream().collect(Collectors.groupingBy(Attribute::getDomain));
        byDomain.keySet().stream().sorted().forEach(domain -> {
            result.append(domain);
            result.append(":");
            result.append("\n");
            byDomain.get(domain).stream().sorted(Comparator.comparing(Attribute::getProperty)).forEach(attr -> {
                result.append("\t");
                result.append(" - ");
                result.append(attr.getProperty());
                result.append(": ");
                result.append(attr.getAttributeClass().getSimpleName());
                result.append("\n");
            });
        });

        return result.toString();
    }
}
