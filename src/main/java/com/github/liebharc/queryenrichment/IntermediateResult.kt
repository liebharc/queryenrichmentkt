package com.github.liebharc.queryenrichment

import java.util.HashMap
import java.util.function.Supplier

/**
 * Intermediate results which are created while evaluating a single row returned by the query.
 */
class IntermediateResult {

    /** The query result  */
    private var queryResult: List<Any?>? = null

    /** Pointer to the next query result  */
    private var queryResultPos = 0

    /** Results for the current row  */
    private var results: MutableMap<Attribute<*>, Any?> = HashMap()

    /** Results of constant steps  */
    private var constants: Map<Attribute<*>, Any?> = HashMap()

    /** Filters set this value to false if a row should be skipped  */
    var isContinueProcessing = true
        private set

    /**
     * Add a result from a step. Steps must produce results.
     */
    fun <T> add(step: Step<T>, result: T) {
        results[step.attribute] = result
    }

    /**
     * Add a result for an attribute. Used to allow to direct communication between steps.
     * Prefer to use [.add] where possible.
     */
    fun <T> add(attribute: Attribute<T>, result: T) {
        results[attribute] = result
    }

    /**
     * Returns the value of an attribute. If the attribute is a dependency of the current step then it must be present.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(attribute: Attribute<T>): T? {
        val constant = constants[attribute] as T
        return constant ?: results[attribute] as T

    }

    /**
     * Returns the value of an attribute or inserts a new value. As [.add] this is used
     * for direct communication between steps and [.get] should be preferred for all other cases.
     */
    fun <T> getOrCreate(attribute: Attribute<T>, onMissSupplier: Supplier<T>): T {
        val result = this[attribute]
        if (result != null) {
            return result
        }

        val newInstance = onMissSupplier.get()
        this.add(attribute, newInstance)
        return newInstance
    }

    /**
     * Adds the result of a query. Java auto conversions will be performed.
     */
    fun addFromQuery(step: Step<*>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.cast(step.attribute.attributeClass, queryResult?.get(queryResultPos))
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addLongFromQuery(step: Step<Long>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castLong(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addIntegerFromQuery(step: Step<Int>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castInteger(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addShortFromQuery(step: Step<Short>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castShort(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addBooleanFromQuery(step: Step<Boolean>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castBoolean(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addFloatFromQuery(step: Step<Float>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castFloat(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than [.addFromQuery].
     */
    fun addDoubleFromQuery(step: Step<Double>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castDouble(queryResult!![queryResultPos])
        this.nextColumn()
    }

    /**
     * Adds the result of a query skipping all Java auto conversions.
     */
    fun addObjectFromQuery(step: Step<*>) {
        val attribute = step.attribute
        results[attribute] = ClassCasts.castObject(step.attribute.attributeClass, queryResult!![queryResultPos])
        this.nextColumn()
    }

    private fun nextColumn() {
        queryResultPos++
    }

    /**
     * Called before the next row is processed.
     * @param row Row results from a query.
     */
    fun nextRow(row: List<Any?>) {
        this.clear()
        this.queryResult = row
    }

    private fun clear() {
        results.clear()
        queryResultPos = 0
        isContinueProcessing = true
    }

    /**
     * Called by filters to stop the processing of a row.
     */
    fun stopProcessing() {
        isContinueProcessing = false
    }

    /**
     * Called after all constants steps have been executed.
     */
    fun markCurrentResultAsConstant() {
        constants = results
        results = HashMap()
    }
}
