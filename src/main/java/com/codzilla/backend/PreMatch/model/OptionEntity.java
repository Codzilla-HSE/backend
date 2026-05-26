package com.codzilla.backend.PreMatch.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionEntity {
    Category category;
    String banObject;

    public OptionEntity(Category category, String banObject) {
        this.category = category;
        this.banObject = banObject;
    }
}
