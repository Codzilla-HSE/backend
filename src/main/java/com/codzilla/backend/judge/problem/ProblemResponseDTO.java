package com.codzilla.backend.judge.problem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProblemResponseDTO {

    Long id;
    String name;
    Integer timeLimit;
    Integer memoryLimit;
    boolean hasStatement;
    int testCount;
    String complexity;
    String taskType;
}
