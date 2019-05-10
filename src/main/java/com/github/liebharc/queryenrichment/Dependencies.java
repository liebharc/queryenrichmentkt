package com.github.liebharc.queryenrichment;

import java.util.*;

/**
 * Utility class with some default dependencies.
 */
public class Dependencies {
    /** Singleton for no dependency */
    private static final NoDependency noDependency = new NoDependency();

    private Dependencies() {
        // Utility class
    }

    /** A step has no dependencies */
    public static Dependency noDependencies() {
        return noDependency;
    }

    /** A step requires all of the given attributes */
    public static Dependency requireAll(Attribute<?>... attributes) {
        return new RequireAll(Arrays.asList(attributes));
    }

    /** A step requires a single attribute */
    public static Dependency require(Attribute<?> attribute) {
        return new RequireAll(Collections.singletonList(attribute));
    }

    /** A step requires one of many attributes */
    public static Dependency requireOneOf(Attribute<?>... attributes) {
        return new RequireOneOf(Arrays.asList(attributes));
    }

    private static class RequireOneOf implements Dependency {

        private final Collection<Attribute<?>> attributes;

        private RequireOneOf(Collection<Attribute<?>> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Attribute<?>> available) {
            if (this.isEmpty()) {
                return Collections.emptyList();
            }

            final Optional<Attribute<?>> any = attributes.stream().filter(available::contains).findAny();
            if (any.isPresent()) {
                return Collections.singletonList(any.get());
            } else {
                // We have no match at all, inform the caller about one of the dependencies as this is the minimum
                // we require
                return Collections.singletonList(attributes.iterator().next());
            }
        }

        @Override
        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> available) {
            if (this.isEmpty()) {
                return true;
            }

            return this.attributes.stream().anyMatch(available::contains);
        }
    }


    private static class RequireAll implements Dependency {

        private final Collection<Attribute<?>> attributes;

        private RequireAll(Collection<Attribute<?>> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Attribute<?>> available) {
            return this.attributes;
        }

        @Override
        public boolean isEmpty() {
            return attributes.isEmpty();
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> available) {
            return available.containsAll(this.attributes);
        }
    }

    private static class NoDependency implements Dependency {

        @Override
        public Collection<Attribute<?>> getMinimalRequiredAttributes(Collection<Attribute<?>> available) {
            return Collections.emptyList();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean isOkay(Set<Attribute<?>> constantAttributes) {
            return true;
        }
    }
}
