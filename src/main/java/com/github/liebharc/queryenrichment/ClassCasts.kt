package com.github.liebharc.queryenrichment

import java.sql.Timestamp

/**
 * Utility class which defines some dynamic class casts under consideration of automatic Java conversions.
 */
object ClassCasts {

    /**
     * Cast the value to the given class. Also supports the most crucial widening primitive conversions.
     */
    fun cast(clazz: Class<*>, value: Any): Any {
        if (clazz == Long::class.java) {
            return castLong(value)
        }

        if (clazz == Int::class.java) {
            return castInteger(value)
        }

        if (clazz == Double::class.java) {
            return castDouble(value)
        }

        return if (clazz == Boolean::class.java) {
            castBoolean(value)
        } else castObject(clazz, value)

    }


    fun castFloat(value: Any): Any {
        return castObject(Float::class.java, value)
    }

    fun castDouble(value: Any): Any {
        return value as? Double ?: ((value as? Float)?.toDouble() ?: castObject(Double::class.java, value))

    }

    fun castInteger(value: Any): Any {
        return value as? Int ?: ((value as? Short)?.toInt() ?: castObject(Int::class.java, value))

    }

    fun castShort(value: Any): Any {
        return castObject(Short::class.java, value)
    }

    fun castLong(value: Any): Any {
        return value as? Long ?: ((value as? Int)?.toLong() ?: ((value as? Short)?.toLong()
                ?: ((value as? Timestamp)?.time ?: castObject(Long::class.java, value))))

    }

    fun castBoolean(value: Any): Any {
        if (value is Boolean) {
            return value
        }

        if (value is Short) {
            return value.toInt() != 0
        }

        if (value is Int) {
            return value != 0
        }

        return if (value is Long) {
            value != 0L
        } else castObject(Boolean::class.java, value)

    }

    fun castObject(clazz: Class<*>, value: Any): Any {
        return clazz.cast(value)
    }
}// Utility class
