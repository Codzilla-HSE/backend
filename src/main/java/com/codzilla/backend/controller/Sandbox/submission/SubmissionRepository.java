package com.codzilla.backend.controller.Sandbox.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    // Находим все сабмиты, которые еще не завершены
    List<Submission> findAllByStatus(Submission.Status status);
}
