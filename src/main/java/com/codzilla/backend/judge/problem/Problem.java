package com.codzilla.backend.judge.problem;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "problems")
@Data
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID задачи в Artefactik0 (для ALGO) или SqlService (для SQL)
    private Long externalId;

    private String name;

    @Enumerated(EnumType.STRING)
    private ProblemType type;

    @Enumerated(EnumType.STRING)
    private ProblemLevel level;

    public enum ProblemType {
        ALGO, SQL
    }

    public enum ProblemLevel {
        EASY, MEDIUM, HARD
    }
}