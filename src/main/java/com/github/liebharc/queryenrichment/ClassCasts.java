package com.github.liebharc.queryenrichment;

import java.sql.Timestamp;

/**
 * Utility class which defines some dynamic class casts under consideration of automatic Java conversions.
 */
public class ClassCasts {

    private ClassCasts() {
        // Utility class
    }

    /**
     * Cast the value to the given class. Also supports the most crucial widening primitive conversions.
     */
    public static Object cast(Class<?> clazz, Object value) {
        if (clazz == Long.class) {
            return castLong(value);
        }

        if (clazz == Integer.class) {
            return castInteger(value);
        }

        if (clazz == Double.class) {
            return castDouble(value);
        }

        if (clazz == Boolean.class) {
            return castBoolean(value);
        }

        return castObject(clazz, value);
    }


    public static Object castFloat(Object value) {
        return castObject(Float.class, value);
    }

    public static Object castDouble(Object value) {
        if (value instanceof Double) {
            return value;
        }

        if (value instanceof Float) {
            return (double)((float)value);
        }

        return castObject(Double.class, value);
    }

    public static Object castInteger(Object value) {
        if (value instanceof Integer) {
            return value;
        }

        if (value instanceof Short) {
            return (int) ((short) value);
        }

        return castObject(Integer.class, value);
    }

    public static Object castShort(Object value) {
        return castObject(Short.class, value);
    }

    public static Object castLong(Object value) {
        if (value instanceof Long) {
            return value;
        }

        if (value instanceof Integer) {
            return (long) ((int) value);
        }

        if (value instanceof Short) {
            return (long) ((short) value);
        }

        if (value instanceof Timestamp) {
            return ((Timestamp)value).getTime();
        }

        return castObject(Long.class, value);
    }

    public static Object castBoolean(Object value) {
        if (value instanceof Boolean) {
            return value;
        }

        if (value instanceof Short) {
            return  ((short) value) != 0;
        }

        if (value instanceof Integer) {
            return ((int) value) != 0;
        }

        if (value instanceof Long) {
            return ((long)value) != 0;
        }

        return castObject(Boolean.class, value);
    }

    public static Object castObject(Class<?> clazz, Object value) {
        return clazz.cast(value);
    }
}
