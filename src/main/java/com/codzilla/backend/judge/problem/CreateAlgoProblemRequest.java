package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
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
    private ProblemType type;
    private Problem.ProblemLevel level;
}