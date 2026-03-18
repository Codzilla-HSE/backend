package com.codzilla.backend.test;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tests")
public class Test {

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