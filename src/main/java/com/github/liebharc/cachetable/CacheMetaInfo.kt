package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import kotlin.reflect.KClass

data class CacheMetaInfo<K: Any, V: Any>(val key: KClass<K>, val value: KClass<V>, val cache: Cache<K, V>)