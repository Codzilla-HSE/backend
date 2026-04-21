package com.codzilla.backend.controller.Sandbox.problem;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "problem_tests")
public class ProblemTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long problemId;
    private int testIndex;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;
}