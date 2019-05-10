package com.github.liebharc.queryenrichment;

import java.io.Serializable;
import java.util.Objects;

/**
 * An attribute which can be queried.
 * @param <T> Attribute type
 */
public class Attribute<T> implements Serializable {

    private static final long serialVersionUID = -3323488022561687505L;
    /** Class of the attribute type */
    private final Class<T> attributeClass;
    /** The domain ob the attribute, could be a database table or an object type */
    private final String domain;
    /** The property of the attribute, could be a database column or an object property */
    private final String property;

    public Attribute(Class<T> attributeClass, String domain, String property) {
        this.attributeClass = attributeClass;
        this.domain = domain;
        this.property = property;
    }

    public Class<T> getAttributeClass() {
        return attributeClass;
    }

    public String getDomain() {
        return domain;
    }

    public String getProperty() {
        return property;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attribute attribute = (Attribute) o;
        return Objects.equals(domain, attribute.domain) &&
                Objects.equals(property, attribute.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, property);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "domain='" + domain + '\'' +
                ", property='" + property + '\'' +
                '}';
    }
}
