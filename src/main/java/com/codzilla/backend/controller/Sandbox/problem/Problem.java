package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.PreMatch.model.Option;
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

    public enum ProblemType implements Option {
        ALGORITHM, DATA_STRUCTURES, MATH
    }

    public enum ProblemLevel implements Option {
        EASY, MEDIUM, HARD
    }
}