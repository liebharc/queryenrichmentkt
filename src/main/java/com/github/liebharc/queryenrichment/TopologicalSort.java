package com.github.liebharc.queryenrichment;

import java.util.*;

/**
 * Implements the topological sort of steps.
 */
public class TopologicalSort {

    /** Singleton */
    public static final TopologicalSort INSTANCE = new TopologicalSort();

    private TopologicalSort() {
        // Singleton
    }

    /**
     * Sorts steps according to their dependencies.
     * @param source Steps to be sorted
     * @param attributeToStep Allows to quickly find the step for an attribute.
     * @return Sorted steps
     */
    public<TParameter> List<ExecutableStep<?, TParameter>> sort(Collection<ExecutableStep<?, TParameter>> source, Map<Attribute<?>, ExecutableStep<?, TParameter>> attributeToStep) {
        final List<ExecutableStep<?, TParameter>> sorted = new ArrayList<>();
        final Set<Attribute<?>> visitedSet = new HashSet<>();

        for (ExecutableStep<?, TParameter> item : source) {
            this.visit(item, visitedSet, sorted, attributeToStep);
        }

        return sorted;
    }

    private<TParameter> void visit(ExecutableStep<?, TParameter> item, Set<Attribute<?>> visited, List<ExecutableStep<?, TParameter>> sorted, Map<Attribute<?>, ExecutableStep<?, TParameter>> attributeToSelector) {
        if (visited.contains(item.getAttribute())) {
            if (!sorted.contains(item)) {
                throw new IllegalArgumentException("Cyclic dependency found, stopped at " + item);
            }
        }
        else {
            visited.add(item.getAttribute());

            Dependency dependencies = item.getDependencies();
            for (Attribute<?> dependency : dependencies.getMinimalRequiredAttributes(visited)) {
                ExecutableStep<?, TParameter> stepDependency = attributeToSelector.get(dependency);
                if (stepDependency == null) {
                    throw new IllegalArgumentException("Unresolved dependency found. " + item + " requires " + dependency);
                }

                this.visit(stepDependency, visited, sorted, attributeToSelector);
            }


            sorted.add(item);
        }
    }
}
