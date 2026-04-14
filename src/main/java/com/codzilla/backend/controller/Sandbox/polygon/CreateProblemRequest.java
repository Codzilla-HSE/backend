package com.codzilla.backend.controller.Sandbox.polygon;

import com.codzilla.backend.controller.Sandbox.problem.Problem;
import lombok.Data;
import java.util.List;

@Data
public class CreateProblemRequest {
    private String name;
    private Problem.ProblemType type;
    private Problem.ProblemLevel level;
    private List<TestCase> tests;

    @Data
    public static class TestCase {
        private String input;
        private String output;
    }
}