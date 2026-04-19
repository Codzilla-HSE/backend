package com.codzilla.backend.controller.Sandbox.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Optional<Submission> findByJudge0Token(String token);
    List<Submission> findAllByStatus(Submission.Status status);
//    Submission findById(Long id);
}
