package com.codzilla.backend.submission;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long problemId;
    private int languageId;

    @Column(columnDefinition = "BYTEA")
    private byte[] sourceCode;

    private String status;
    private String submissionUuid;
}