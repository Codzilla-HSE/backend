package com.codzilla.backend.MatchRoom;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Option;
import com.codzilla.backend.PreMatch.model.OptionStatusDTO;
import com.codzilla.backend.judge.problem.Problem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

@Slf4j
@Entity
@Getter
@Setter
public class Match {

    public enum Status {
        DRAFTING,
        LIVE,
        FINISHED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    UUID firstUserId;

    @JdbcTypeCode(SqlTypes.UUID)
    UUID secondUserId;

    @JdbcTypeCode(SqlTypes.UUID)
    UUID winnerId;

    Instant finishedAt;

    boolean ratingApplied = false;

    Integer firstUserRating;

    Integer secondUserRating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    Map<Category, String> options;

    Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    void setOptionsOfDraftSession(DraftSession draftSession) {
        Map<Category, String> new_options = new HashMap<>();
        for (var categoryDTO : draftSession.getOptionsStates()) {
            new_options.put(categoryDTO.getCategory(),
                        categoryDTO.getOptions().stream()
                                   .filter(Predicate.not(OptionStatusDTO::isBanned)).findFirst()
                                   .get().getOption()
            );
        }
        options = new_options;
        log.info("Set options for match: {}", options);
    }

    public UUID opponentOf(UUID userId) {
        if (userId.equals(firstUserId)) return secondUserId;
        if (userId.equals(secondUserId)) return firstUserId;
        return null;
    }

    public Match(UUID firstUserId, UUID secondUserId) {
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

    public Match() {
    }
}
