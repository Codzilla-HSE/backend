package com.codzilla.backend.controller.Sandbox.polygon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)   // <-- добавить
public class PolygonProblem {
    private String status;
    private String comment;                    // <-- добавить
    private List<Test> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Test {
        private int index;
        private String input;
        private String output;
        private String inputBase64;            // <-- Polygon возвращает base64
    }
}