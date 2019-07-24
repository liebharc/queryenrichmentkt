package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import java.util.*
import java.util.function.Function
import java.util.stream.Stream
import kotlin.reflect.KClass

open class SingleIdCacheMetaInfo(override val key: Class<out Any>, override val value: Class<out Any>, private val cache: Cache<out Any, out Any>) : ICacheMetaInfo {
    override fun createFieldAccessor(colName: String): Function<Any, Any>? {
       return null;
    }

    override fun size(): Long {
        return cache.size();
    }

    override fun getOrNull(key: Array<Any?>): Any? {
        val result = cache.getIfPresent(key.get(0)!!);
        if (result == null) {
            return null;
        }

        return result;
    }

    override fun getAll(): Stream<out MutableMap.MutableEntry<out Any, out Any>> {
        return cache.asMap().entries.stream();
    }

    override fun getNumberOfIndexColumns(): Int {
        return 1;
    }
}