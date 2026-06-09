package com.codzilla.backend.judge.problem;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query(value = "SELECT * FROM problems WHERE problem_type = :problemType AND problem_level = :problemLevel ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Problem getRandomProblem(@Param("problemType") String problemType, @Param("problemLevel") String problemLevel);
}