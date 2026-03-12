package com.codzilla.backend.controller.Sandbox.polygon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data

public class PolygonProblem {
    private String status;
    private List<Test> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Test {
        private int index;
        private String input;
        private String output;
    }
}
