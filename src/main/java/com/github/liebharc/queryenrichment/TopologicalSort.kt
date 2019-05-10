package com.github.liebharc.queryenrichment

import java.util.*

/**
 * Implements the topological sort of steps.
 */
object TopologicalSort
{

    /**
     * Sorts steps according to their dependencies.
     * @param source Steps to be sorted
     * @param attributeToStep Allows to quickly find the step for an attribute.
     * @return Sorted steps
     */
    fun <TParameter> sort(source: Collection<ExecutableStep<*, TParameter>>, attributeToStep: Map<Attribute<*>, ExecutableStep<*, TParameter>>): List<ExecutableStep<*, TParameter>> {
        val sorted = ArrayList<ExecutableStep<*, TParameter>>()
        val visitedSet = HashSet<Attribute<*>>()

        for (item in source) {
            this.visit(item, visitedSet, sorted, attributeToStep)
        }

        return sorted
    }

    private fun <TParameter> visit(item: ExecutableStep<*, TParameter>, visited: MutableSet<Attribute<*>>, sorted: MutableList<ExecutableStep<*, TParameter>>, attributeToSelector: Map<Attribute<*>, ExecutableStep<*, TParameter>>) {
        if (visited.contains(item.attribute)) {
            if (!sorted.contains(item)) {
                throw IllegalArgumentException("Cyclic dependency found, stopped at $item")
            }
        } else {
            visited.add(item.attribute)

            val dependencies = item.dependencies
            for (dependency in dependencies.getMinimalRequiredAttributes(visited)) {
                val stepDependency = attributeToSelector[dependency]
                        ?: throw IllegalArgumentException("Unresolved dependency found. $item requires $dependency")

                this.visit(stepDependency, visited, sorted, attributeToSelector)
            }


            sorted.add(item)
        }
    }
}
