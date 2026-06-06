package com.codzilla.backend.judge.problem;

import lombok.Data;
import java.util.List;

@Data
public class CreateAlgoProblemRequest {
    private String name;
    private Integer timeLimit;
    private Integer memoryLimit;
    private String statement;
    private String generatorCode;
    private List<String> inputs;
    private Problem.ProblemLevel level;
}