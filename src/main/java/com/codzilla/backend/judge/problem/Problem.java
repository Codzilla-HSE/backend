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

    private String name;

    // ID задачи во внешнем сервисе:
    // ALGO → id в Artefactik0
    // SQL  → id задачи в SqlService
    private Long externalId;

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