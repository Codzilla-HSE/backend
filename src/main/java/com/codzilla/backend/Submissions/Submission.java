package com.codzilla.backend.Submissions;

import java.util.UUID;

public record Submission(
        UUID id,
        UUID userId,
        byte[] content,
        Language language
) {
}

enum Language { CPP, PY, JAVA }