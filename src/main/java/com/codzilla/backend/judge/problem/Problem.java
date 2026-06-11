package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
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
    private Long externalId;

    @Enumerated(EnumType.STRING)
    private ProblemType type = ProblemType.ALGORITHM;

    @Enumerated(EnumType.STRING)
    private ProblemLevel level ;

    public enum ProblemLevel {
        EASY, MEDIUM, HARD
    }
}