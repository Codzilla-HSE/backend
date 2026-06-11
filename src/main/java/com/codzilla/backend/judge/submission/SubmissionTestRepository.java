package com.codzilla.backend.judge.submission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionTestRepository extends JpaRepository<SubmissionTest, Long> {
    List<SubmissionTest> findAllBySubmissionIdOrderByTestIndex(Long submissionId);
    List<SubmissionTest> findAllByStatus(SubmissionTest.Status status);
}