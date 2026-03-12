package com.codzilla.backend.controller.Sandbox.polygon;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
public class PolygonResponse {
    private String status;
    private String comment;
    private ProblemResult result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProblemResult {
        private Long id;
        private String owner;
        private String name;
    }
}