package com.codzilla.backend.controller.Sandbox.problem;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemTestRepository extends JpaRepository<ProblemTest, Long> {
    List<ProblemTest> findAllByProblemIdOrderByTestIndex(Long problemId);
}