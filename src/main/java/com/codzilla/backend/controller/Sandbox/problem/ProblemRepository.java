package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    @Query(value = "SELECT * FROM problems " +
            "WHERE (type = :problem_type AND level = :problem_level) " +
            "ORDER BY RANDOM() LIMIT 1",
            nativeQuery = true)
    Problem getRandomProblem(@Param("problem_type") String problemType, @Param("problem_level") String problemLevel);
}



