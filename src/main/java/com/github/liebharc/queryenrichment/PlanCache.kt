package com.github.liebharc.queryenrichment

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

import javax.annotation.ParametersAreNonnullByDefault

/**
 * Allows for caching of plan. If caching is used then with each query the cache will be checked if there is already
 * a plan for the given query.
 */
class PlanCache<TParameter>(cacheSize: Int, planBuilder: PlanBuilder<TParameter>) {

    /**
     * The internal cache.
     */
    private val plans: LoadingCache<Request, Plan<TParameter>>

    init {
        plans = CacheBuilder.newBuilder()
                .maximumSize(cacheSize.toLong())
                .build<Request, Plan<TParameter>>(
                        object : CacheLoader<Request, Plan<TParameter>>() {
                            @ParametersAreNonnullByDefault
                            override fun load(request: Request): Plan<TParameter> {
                                return planBuilder.build(request)
                            }
                        }
                )
    }

    fun getOrBuildPlan(request: Request): Plan<TParameter> {
        return this.plans.getUnchecked(request)
    }
}
