package com.codzilla.backend.controller.Sandbox.submission;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "submission_tests")
public class SubmissionTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long submissionId;
    private int testIndex;
    private String judge0Token;

    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_QUEUE;

    @Column(columnDefinition = "TEXT")
    private String actualOutput;

    public enum Status {
        IN_QUEUE, PROCESSING, ACCEPTED, WRONG_ANSWER, COMPILE_ERROR, RUNTIME_ERROR
    }
}