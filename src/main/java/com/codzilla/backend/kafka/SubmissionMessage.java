package com.codzilla.backend.kafka;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionMessage {
    private String submissionId;
    private Long problemId;
    private String sourceCode;
    private int languageId;
}