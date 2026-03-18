package com.codzilla.backend.test;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByProblemIdOrderByTestIndex(Long problemId);
}