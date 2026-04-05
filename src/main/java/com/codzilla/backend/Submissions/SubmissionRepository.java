package com.codzilla.backend.Submissions;

import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository {
    void save(Submission submission);
    Optional<Submission> get(UUID id);
}
