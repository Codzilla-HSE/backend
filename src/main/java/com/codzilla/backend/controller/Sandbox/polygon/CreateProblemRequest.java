package com.codzilla.backend.controller.Sandbox.polygon;

import com.codzilla.backend.controller.Sandbox.problem.Problem;
import lombok.Data;
import java.util.List;

// То что принимаем от пользователя
@Data
public class CreateProblemRequest {
    private String name;           // название задачи
    private Problem.ProblemType type;
    private Problem.ProblemLevel level;
    private List<TestCase> tests;  // список тестов

    @Data
    public static class TestCase {
        private String input;   // "1 2"
        private String output;  // "3"
    }
}