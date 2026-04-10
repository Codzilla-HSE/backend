package com.codzilla.backend.controller.Sandbox.polygon;

import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblem;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PolygonCache {

    private final Cache<String, List<PolygonProblem.Test>> cache =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .maximumSize(1000)
                    .build();

    public List<PolygonProblem.Test> get(String polygonId) {
        return cache.getIfPresent(polygonId);
    }

    public void put(String polygonId, List<PolygonProblem.Test> tests) {
        cache.put(polygonId, tests);
    }

    public void invalidate(String polygonId) {
        cache.invalidate(polygonId);
    }
}