package com.codzilla.backend.judge.problem;

import lombok.Data;

@Data
public class RegisterSqlProblemRequest {
    private String name;
    private Long sqlServiceTaskId;  // id задачи, уже созданной в SqlService
    private Problem.ProblemLevel level;
}