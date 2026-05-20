package com.codzilla.backend.PreMatch.model;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class DraftSessionResponseDTO {
    Status status;
    DraftSession draftSession;
    String error;

    public enum Status {
        ERROR,
        SUCCEED
    }
}
