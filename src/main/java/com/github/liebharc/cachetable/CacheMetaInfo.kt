package com.github.liebharc.cachetable

import com.google.common.cache.Cache
import kotlin.reflect.KClass

data class CacheMetaInfo(val key: Class<out Any>, val value: Class<out Any>, val cache: Cache<out Any, out Any>)