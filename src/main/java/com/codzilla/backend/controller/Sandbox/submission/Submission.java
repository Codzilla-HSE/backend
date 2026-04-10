
package com.codzilla.backend.controller.Sandbox.submission;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "submissions")
@Data
public class Submission {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long problemId;
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String sourceCode;

    private Integer languageId;

    private String judge0Token;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String resultDetails;

    private LocalDateTime createdAt = LocalDateTime.now();


    private int retryCount = 0;
    private LocalDateTime updatedAt = LocalDateTime.now();


    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Status {
        IN_QUEUE, PROCESSING, ACCEPTED, WRONG_ANSWER, COMPILE_ERROR, RUNTIME_ERROR, INTERNAL_ERROR
    }
}