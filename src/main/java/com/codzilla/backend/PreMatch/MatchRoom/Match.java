package com.codzilla.backend.PreMatch.MatchRoom;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.model.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Match {

    enum Status {
        DRAFTING,
        LIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    UUID firstUserId;

    @JdbcTypeCode(SqlTypes.UUID)
    UUID secondUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", columnDefinition = "jsonb")
    HashMap<Category, String> options = new HashMap<>();

    Status status;

    void setOptionsOfDraftSession(DraftSession draftSession) {

        for (var option : draftSession.getRemainOptions().entrySet()) {
            options.put(option.getKey(), option.getValue().stream().findFirst().get());
        }
    }

    Match(UUID firstUserId, UUID secondUserId) {
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

    public Match() {}
}
