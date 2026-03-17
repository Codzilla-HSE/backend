package com.codzilla.backend.Submissions;

import java.util.Optional;

public interface SubmissionRepository {


    void save(Submission submission); // returns id
    Optional<Submission> get(String id);
}
