package com.codzilla.backend.controller.Sandbox.problem;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "problems")
@Data
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String polygonToken;

    @Enumerated(EnumType.STRING)
    private ProblemType type;

    @Enumerated(EnumType.STRING)
    private ProblemLevel level;

    public enum ProblemType {
        ALGORITHM, DATA_STRUCTURES, MATH
    }

    public enum ProblemLevel {
        EASY, MEDIUM, HARD
    }
}