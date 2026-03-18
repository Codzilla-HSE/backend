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

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] sourceCode;  // бинарник исходника

    private String status;  // PENDING, ACCEPTED, WRONG_ANSWER, etc.

    private String submissionUuid;  // для WebSocket
}