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

    private String polygonToken;  // индекс/токен из Polygon

    @Enumerated(EnumType.STRING)
    private ProblemType type;     // ALGORITHM, DATA_STRUCTURES и т.д.

    @Enumerated(EnumType.STRING)
    private ProblemLevel level;   // EASY, MEDIUM, HARD

    public enum ProblemType {
        ALGORITHM, DATA_STRUCTURES, MATH
    }

    public enum ProblemLevel {
        EASY, MEDIUM, HARD
    }
}