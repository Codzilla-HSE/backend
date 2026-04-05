package com.codzilla.backend.Submissions;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.UUID;

public interface SubmissionDBRepository extends JpaRepository<Submission, UUID> {
    ArrayList<Submission> findTop50ByStatus(SubmissionStatus status);
}
