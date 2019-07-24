package com.github.liebharc.cachetable

import java.util.function.Function
import java.util.stream.Stream

interface ICacheMetaInfo {
    val key: Class<out Any>
    val value: Class<out Any>

    fun size(): Long
    fun getValueOrNull(key: Array<Any?>): Any?
    fun getAllValues(): Stream<out MutableMap.MutableEntry<out Any, out Any>>
    fun getNumberOfIndexColumns(): Int
    fun createFieldAccessor(colName: String): Function<Any, Any>?;
}