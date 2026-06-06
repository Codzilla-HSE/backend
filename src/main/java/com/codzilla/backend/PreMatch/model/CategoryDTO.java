package com.codzilla.backend.PreMatch.model;

import com.codzilla.backend.PreMatch.exceptions.DraftSessionException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CategoryDTO {
    Category category;

    HashSet<OptionStatusDTO> options = new HashSet<>();
    boolean isActive = true;

    public CategoryDTO(Category category, boolean isActive) {
        for (var option : category.getOptionClass().getEnumConstants()) {
            options.add(new OptionStatusDTO(option.name(), false));
        }
        this.isActive = isActive;
        this.category = category;
    }


    public void banOption(String option) {
        var optionsStatus = options.stream().filter(optionStatusDTO -> optionStatusDTO.getOption().equals(option)).findAny().orElseThrow();
        if (optionsStatus.isBanned) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.OPTION_ALREADY_BANNED);
        }
        log.info("Setting isBanned: true for : {}", option);
        optionsStatus.setBanned(true);
    }

    public long amountOfRemainOptions() {
        return options.stream().filter(Predicate.not(OptionStatusDTO::isBanned)).count();
    }

    public String anyActiveOption() {
        OptionStatusDTO activeOptionDto = options.stream()
                                                 .filter(option -> !option.isBanned())
                                                 .findAny()
                                                 .orElseThrow(() -> new IllegalStateException("Нет доступных активных опций в категории " + category));


        return activeOptionDto.getOption();
    }

    public void makeRandomBan() {
        var activeOptions = options.stream().filter(Predicate.not(OptionStatusDTO::isBanned)).toList();
        assert activeOptions.size() >= 2;
        int randomIndex = ThreadLocalRandom.current().nextInt(activeOptions.size());
        activeOptions.get(randomIndex).setBanned(true);
    }
}
