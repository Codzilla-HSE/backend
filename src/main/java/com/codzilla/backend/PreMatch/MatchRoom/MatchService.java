package com.codzilla.backend.PreMatch.MatchRoom;


import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionService;
import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final DraftSessionService draftSessionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchSettings matchSettings;


    public UUID startMatch(UUID firstUserId, UUID secondUserId) {
        Match match = new Match(
                firstUserId,
                secondUserId
        );
        match.setStatus(Match.Status.DRAFTING);
        matchRepository.save(match);
        draftSessionService.startDraftSession(
                match.getId(),
                firstUserId,
                secondUserId
        );
        return match.getId();
    }

    public void setOptionsOfDraftSession(UUID matchId, DraftSession draftSession) {
        Match match = matchRepository.findById(matchId).get();
        match.setOptionsOfDraftSession(draftSession);
        matchRepository.save(match);
    }
}
