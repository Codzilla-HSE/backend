package com.codzilla.backend.PreMatch.DraftSession;


import com.codzilla.backend.PreMatch.exceptions.DraftSessionException;
import com.codzilla.backend.PreMatch.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

@Entity
@Table(name = "draft_session")
@Getter
@Setter
@NoArgsConstructor
public class DraftSession {

    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    UUID id;

    @JdbcTypeCode(SqlTypes.UUID)
    private UUID firstUserId;

    @JdbcTypeCode(SqlTypes.UUID)
    private UUID secondUserId;

    private Status status = Status.PICKING;

    boolean isFirstUserMove = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_states", columnDefinition = "jsonb")
    List<CategoryDTO> optionsStates = new ArrayList<>();

    public DraftSession(UUID matchId, UUID firstUserId, UUID secondUserId, Category initCategory) {
        this.firstUserId = firstUserId;
        this.secondUserId = secondUserId;
        this.id = matchId;

        addCategory(initCategory);
    }

    void addCategory(Category category) {
        CategoryDTO categoryDTO = new CategoryDTO(
                category,
                true
        );
        optionsStates.add(categoryDTO);
    }

    void banOption(Category categoryOfOption, String optionToBan,
                   Map<String, Category> categorySequence)
            throws DraftSessionException {
        var categoryDto = optionsStates.getLast();
        if (categoryDto.getCategory() != categoryOfOption) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.CATEGORY_NOT_EXISTS_IN_SESSION);
        }

        if (Arrays.stream(categoryOfOption.getOptionClass().getEnumConstants())
                  .noneMatch((option -> option.name().equals(optionToBan)))) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.OPTION_DO_NOT_EXISTS);
        }

        if (!categoryDto.isActive()) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.CATEGORY_IS_FINISHED);
        }
        categoryDto.banOption(optionToBan);
        isFirstUserMove = !isFirstUserMove;
        if (categoryDto.amountOfRemainOptions() == 1) {
            categoryDto.setActive(false);
            var lastOption = categoryDto.anyActiveOption();
            var nextCategory = categorySequence.get(lastOption);
            if (nextCategory == null) {
                status = Status.FINISHED;
                return;
            }
            addCategory(nextCategory);
        }
    }

    void makeRandomBan(Map<String, Category> categorySequence) {
        var activeCategoryDTO = optionsStates.getLast();
        assert activeCategoryDTO.isActive();
        var notBannedOptions = activeCategoryDTO.getOptions().stream()
                                                .filter(Predicate.not(OptionStatusDTO::isBanned))
                                                .toList();
        assert notBannedOptions.size() >= 2;

        int randomIndex = ThreadLocalRandom.current().nextInt(notBannedOptions.size());
        banOption(activeCategoryDTO.getCategory(), notBannedOptions.get(randomIndex).getOption(), categorySequence);
    }

    public enum Status {
        PICKING,
        FINISHED
    }
}
