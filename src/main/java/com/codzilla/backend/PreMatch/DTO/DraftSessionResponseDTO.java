package com.codzilla.backend.PreMatch.DTO;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter
public class DraftSessionResponseDTO {
    DraftSession draftSession;
}
