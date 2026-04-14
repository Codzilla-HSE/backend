package com.codzilla.backend.controller.Sandbox.polygon;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolygonProblemService {

    private final PolygonClient polygonClient;
    private final Cache<String, List<PolygonProblem.Test>> cache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();


    public List<PolygonProblem.Test> getTests(String polygonId) {
        try {
            return cache.get(polygonId, () -> {
                log.info("Fetching fresh tests for problem {} from Polygon", polygonId);
                PolygonProblem polygonProblem = polygonClient.getProblemTests(polygonId);
                List<PolygonProblem.Test> tests = polygonProblem.getResult();
                if (tests == null || tests.isEmpty()) {
                    throw new RuntimeException("Polygon returned empty tests for problem " + polygonId);
                }
                return tests;
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to load tests for problem " + polygonId, e);
        }
    }

    public void updateProblemInCache(String polygonId) {
        cache.invalidate(polygonId);
        log.info("Problem {} invalidated", polygonId);
    }


    public void addTestToPolygon(String polygonId, int index, String input, String output) {
        polygonClient.saveTest(polygonId, index, input, output);
        cache.invalidate(polygonId);
    }
}