package com.codzilla.backend.PreMatch.DraftSession;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DraftSessionRepository extends JpaRepository<DraftSession, UUID> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DraftSession d where d.id = :id")
    Optional<DraftSession> findByIdWithLock(@Param("id") UUID id);
}
