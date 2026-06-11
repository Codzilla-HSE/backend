package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    @Query(value = "SELECT * FROM problems WHERE type = :problemType AND level = :problemLevel ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Problem getRandomProblem(@Param("problemType") String problemType,
                             @Param("problemLevel") String problemLevel);

    Optional<Problem> findByExternalIdAndType(Long externalId, ProblemType type);
}