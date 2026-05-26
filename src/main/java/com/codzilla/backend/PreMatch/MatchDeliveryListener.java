package com.codzilla.backend.PreMatch;

import com.codzilla.backend.PreMatch.DTO.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.MatchRoom.Match;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import com.codzilla.backend.PreMatch.events.DraftSessionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class MatchDeliveryListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MatchSettings matchSettings;
    private final MatchService matchService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDraftUpdate(DraftSessionUpdatedEvent event) {
        messagingTemplate.convertAndSend(
                matchSettings.getWebSocketMatchDestination(event.getDraftSession().getId()),
                new WebSocketDTO(
                        WebSocketDTO.Status.DRAFT,
                        new DraftSessionResponseDTO(
                                event.getDraftSession()
                        )
                )
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDraftSessionFinished(DraftSessionFinishedEvent event) {
        matchService.setOptionsOfDraftSession(
                event.getDraftSession().getId(),
                event.getDraftSession()
        );
        messagingTemplate.convertAndSend(
                matchSettings.getWebSocketMatchDestination(event.getDraftSession().getId()),
                new WebSocketDTO(
                        WebSocketDTO.Status.MATCH_STARTED_REDIRECT,
                        null
                )
        );
    }
}
