package com.codzilla.backend.judge.problem;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
public class ProblemResponseDTO {

    Long id;
    String name;
    Integer timeLimit;
    Integer memoryLimit;
    boolean hasStatement;
    int testCount;
    LocalDateTime createdAt;
    String complexity;
    String taskType;
}
