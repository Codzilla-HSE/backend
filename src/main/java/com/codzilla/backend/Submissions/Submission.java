package com.codzilla.backend.Submissions;

import java.util.UUID;
import com.codzilla.backend.Submissions.Language;

public record Submission(
        UUID id,
        UUID userId,
        String s3Path,
        Language language
) {
}

enum Language {CPP, PY, JAVA}