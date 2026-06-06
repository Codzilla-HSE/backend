package com.codzilla.backend.judge.problem;

import lombok.Data;

@Data
public class RegisterSqlProblemRequest {
    private String name;
    private Long sqlServiceTaskId;
    private Problem.ProblemLevel level;
}