package com.codzilla.backend.Matchmaking;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class MatchFoundEvent extends ApplicationEvent {

    private final UUID playerOneId;
    private final UUID playerTwoId;

    public MatchFoundEvent(Object source, UUID playerOneId, UUID playerTwoId) {
        super(source);
        this.playerOneId = playerOneId;
        this.playerTwoId = playerTwoId;
    }

    public UUID getPlayerOneId() { return playerOneId; }
    public UUID getPlayerTwoId() { return playerTwoId; }
}
