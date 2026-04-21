package com.codzilla.backend.controller.Sandbox.submission;

import java.time.LocalDateTime;

public record SubmissionResponseDTO(
        Long id,
        Long problemId,
        Integer languageId,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SubmissionResponseDTO fromEntity(Submission submission) {
        return new SubmissionResponseDTO(
                submission.getId(),
                submission.getProblemId(),
                submission.getLanguageId(),
                submission.getStatus().name(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}