package com.codzilla.backend.judge.problem;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemResponseDTO {

    private Long id;
    private String name;
    private Integer timeLimit;
    private Integer memoryLimit;
    private boolean hasStatement;
    private int testCount;
    private LocalDateTime createdAt;
    private String complexity;
    private String taskType;
}
