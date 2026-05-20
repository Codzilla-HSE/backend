package com.codzilla.backend.PreMatch.DraftSession;


import com.codzilla.backend.PreMatch.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.*;

@Entity
@Table(name = "lobby")
@Getter
@Setter
public class DraftSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.UUID)
    UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    private UUID firstUserId;

    @JdbcTypeCode(SqlTypes.UUID)
    private UUID secondUserId;

    private Status status = Status.PICKING;

    boolean isFirstUserMove;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "remain_options", columnDefinition = "jsonb")
    Map<Category, Set<String>> remainOptions = new HashMap<>();

    public DraftSession() {
        isFirstUserMove = true;
        for (var category : Category.values()) {
            remainOptions.put(category, new HashSet<>());
            for (var option : category.getEnumClass().getEnumConstants()) {
                remainOptions.get(category).add(option.name());
            }
        }
    }


    public DraftSession(UUID firstUserId, UUID secondUserId) {
        this();
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
    }

    public enum Status {
        PICKING,
        FINISHED
    }
}
