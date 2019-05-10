package com.github.liebharc.queryenrichment;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Allows for caching of plan. If caching is used then with each query the cache will be checked if there is already
 * a plan for the given query.
 */
public class PlanCache {

    /**
     * The internal cache.
     */
    private final LoadingCache<Request, Plan> plans;

    public PlanCache(int cacheSize, PlanBuilder planBuilder) {
         plans = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .build(
                        new CacheLoader<Request, Plan>() {
                            @Override
                            @ParametersAreNonnullByDefault
                            public Plan load(Request request) {
                                return planBuilder.build(request);
                            }
                        }
                );
    }

    public Plan getOrBuildPlan(Request request) {
        return this.plans.getUnchecked(request);
    }
}
