package com.codzilla.backend.User;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u WHERE u.email = :email")
    Optional<UUID> findIdByEmail(@Param("email") String email);

    List<User> findAllByOrderByRatingDescIdAsc(Pageable pageable);

    List<User> findAllByOrderByRatingAscIdDesc(Pageable pageable);

    long countByRatingGreaterThan(int rating);
}
