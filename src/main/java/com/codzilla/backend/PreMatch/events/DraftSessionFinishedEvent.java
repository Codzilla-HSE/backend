package com.codzilla.backend.PreMatch.events;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import lombok.Getter;

@Getter
public class DraftSessionFinishedEvent {
    public DraftSessionFinishedEvent(DraftSession draftSession) {
        this.draftSession = draftSession;
    }

    DraftSession draftSession;
}
