package com.codzilla.backend.controller.Sandbox.polygon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolygonProblemService {

    private final PolygonClient polygonClient;
    private final PolygonCache polygonCache;


    public List<PolygonProblem.Test> getTests(String polygonId) {
        List<PolygonProblem.Test> cachedTests = polygonCache.get(polygonId);
        if (cachedTests != null) {
            return cachedTests;
        }

        log.info("Fetching fresh tests for problem {} from Polygon", polygonId);
        PolygonProblem polygonProblem = polygonClient.getProblemTests(polygonId);
        List<PolygonProblem.Test> tests = polygonProblem.getResult();

        if (tests != null && !tests.isEmpty()) {
            polygonCache.put(polygonId, tests);
        }
        return tests;
    }


    public void updateProblemInCache(String polygonId) {
        polygonCache.invalidate(polygonId);
        log.info("Problem {} invalidated. It will be re-synced on next request.", polygonId);
    }


    public void addTestToPolygon(String polygonId, int index, String input, String output) {
        polygonClient.saveTest(polygonId, index, input, output);
        polygonCache.invalidate(polygonId);
    }
}