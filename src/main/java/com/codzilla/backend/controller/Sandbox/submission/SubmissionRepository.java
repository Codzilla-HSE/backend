package com.codzilla.backend.controller.Sandbox.submission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByJudge0Token(String token);
    List<Submission> findAllByStatus(Submission.Status status);

    List<Submission> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<Submission> findFirstByUserIdOrderByUpdatedAtDesc(UUID userId);
}
