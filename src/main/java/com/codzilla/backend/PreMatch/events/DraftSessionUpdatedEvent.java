package com.codzilla.backend.PreMatch.events;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import lombok.Getter;

@Getter
public class DraftSessionUpdatedEvent {
    public DraftSessionUpdatedEvent(DraftSession draftSession) {
        this.draftSession = draftSession;
    }

    DraftSession draftSession;
}
