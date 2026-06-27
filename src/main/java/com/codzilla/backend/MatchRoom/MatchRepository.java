package com.codzilla.backend.MatchRoom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("""
            SELECT m FROM Match m
            WHERE m.status = com.codzilla.backend.MatchRoom.Match.Status.FINISHED
              AND (m.firstUserId = :userId OR m.secondUserId = :userId)
            ORDER BY m.finishedAt DESC
            """)
    List<Match> findFinishedByUser(@Param("userId") UUID userId);
}
