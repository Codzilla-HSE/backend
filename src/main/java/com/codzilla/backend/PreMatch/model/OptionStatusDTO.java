package com.codzilla.backend.PreMatch.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.common.quota.ClientQuotaAlteration;

@Getter
@Setter
@NoArgsConstructor
public class OptionStatusDTO {
    String option;
    boolean isBanned = false;

    public OptionStatusDTO(String option, boolean isBanned) {
        this.option = option;
        this.isBanned = isBanned;
    }
}
