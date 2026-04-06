package com.codzilla.backend.Submissions;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface SubmissionDBRepository extends JpaRepository<Submission, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Submission> findTop50ByStatus(SubmissionStatus status);
}
