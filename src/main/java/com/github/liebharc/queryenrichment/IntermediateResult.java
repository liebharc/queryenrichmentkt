package com.github.liebharc.queryenrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Intermediate results which are created while evaluating a single row returned by the query.
 */
public class IntermediateResult {

    /** The query result */
    private List<Object> queryResult;

    /** Pointer to the next query result */
    private int queryResultPos = 0;

    /** Results for the current row */
    private Map<Attribute, Object> results = new HashMap<>();

    /** Results of constant steps */
    private Map<Attribute, Object> constants = new HashMap<>();

    /** Filters set this value to false if a row should be skipped */
    private boolean continueProcessing = true;

    /**
     * Add a result from a step. Steps must produce results.
     */
    public<T> void add(Step<T> step, T result) {
        results.put(step.getAttribute(), result);
    }

    /**
     * Add a result for an attribute. Used to allow to direct communication between steps.
     * Prefer to use {@link #add(Step, Object)} where possible.
     */
    public<T> void add(Attribute<T> attribute, T result) { results.put(attribute, result); }

    /**
     * Returns the value of an attribute. If the attribute is a dependency of the current step then it must be present.
     */
    @SuppressWarnings("unchecked")
    public<T> T get(Attribute<T> attribute) {
        final T constant = (T) constants.get(attribute);
        if (constant != null) {
            return constant;
        }

        return (T)results.get(attribute);
    }

    /**
     * Returns the value of an attribute or inserts a new value. As {@link #add(Attribute, Object)} this is used
     * for direct communication between steps and {@link #get(Attribute)} should be preferred for all other cases.
     */
    @SuppressWarnings("unchecked")
    public<T> T getOrCreate(Attribute<T> attribute, Supplier<T> onMissSupplier) {
        final T result = this.get(attribute);
        if (result != null) {
            return result;
        }

        final T newInstance = onMissSupplier.get();
        this.add(attribute, newInstance);
        return newInstance;
    }

    /**
     * Adds the result of a query. Java auto conversions will be performed.
     */
    public void addFromQuery(Step<?> step) {
        Attribute<?> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.cast(step.getAttribute().getAttributeClass(), queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addLongFromQuery(Step<Long> step) {
        Attribute<Long> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castLong(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addIntegerFromQuery(Step<Integer> step) {
        Attribute<Integer> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castInteger(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addShortFromQuery(Step<Short> step) {
        Attribute<Short> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castShort(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addBooleanFromQuery(Step<Boolean> step) {
        Attribute<Boolean> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castBoolean(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addFloatFromQuery(Step<Float> step) {
        Attribute<Float> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castFloat(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query assuming a certain data type.
     * Java auto conversions will be performed.
     * This is slightly faster than {@link #addFromQuery(Step)}.
     */
    public void addDoubleFromQuery(Step<Double> step) {
        Attribute<Double> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castDouble(queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    /**
     * Adds the result of a query skipping all Java auto conversions.
     */
    public void addObjectFromQuery(Step<?> step) {
        Attribute<?> attribute = step.getAttribute();
        results.put(attribute, ClassCasts.castObject(step.getAttribute().getAttributeClass(), queryResult.get(queryResultPos)));
        this.nextColumn();
    }

    private void nextColumn() {
        queryResultPos++;
    }

    /**
     * Called before the next row is processed.
     * @param row Row results from a query.
     */
    public void nextRow(List<Object> row) {
        this.clear();
        this.queryResult = row;
    }

    private void clear() {
        results.clear();
        queryResultPos = 0;
        continueProcessing = true;
    }

    public boolean isContinueProcessing() {
        return continueProcessing;
    }

    /**
     * Called by filters to stop the processing of a row.
     */
    public void stopProcessing() {
        continueProcessing = false;
    }

    /**
     * Called after all constants steps have been executed.
     */
    public void markCurrentResultAsConstant() {
        constants = results;
        results = new HashMap<>();
    }
}
