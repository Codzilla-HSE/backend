package com.codzilla.backend.PreMatch;

import com.codzilla.backend.PreMatch.DTO.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.DTO.MatchResultDTO;
import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.events.MatchResultNotifyEvent;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import com.codzilla.backend.PreMatch.events.DraftSessionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
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
    @EventListener
    public void handleDraftSessionFinished(DraftSessionFinishedEvent event) {
        matchService.setUpMatch(
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

    @Async
    @EventListener
    public void handleMatchResult(MatchResultNotifyEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.winnerEmail(),
                "/queue/match-result",
                new WebSocketDTO(
                        WebSocketDTO.Status.MATCH_FINISHED,
                        new MatchResultDTO("WIN", event.winnerNewRating(), event.winnerRatingDelta())
                )
        );

        messagingTemplate.convertAndSendToUser(
                event.loserEmail(),
                "/queue/match-result",
                new WebSocketDTO(
                        WebSocketDTO.Status.MATCH_FINISHED,
                        new MatchResultDTO("LOSE", event.loserNewRating(), event.loserRatingDelta())
                )
        );
    }
}
