package com.github.liebharc.cachetable

import java.util.stream.Stream

interface ICacheMetaInfo {
    val key: Class<out Any>
    val value: Class<out Any>
    fun size(): Long
    fun getOrNull(key: List<Any?>): List<Any?>
    fun getAll(): Stream<out MutableMap.MutableEntry<out Any, out Any>>
    fun getNumberOfIndexColumns(): Long
}