package com.codzilla.backend.PreMatch.MatchRoom;


import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionService;
import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import com.codzilla.backend.controller.Sandbox.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final DraftSessionService draftSessionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MatchSettings matchSettings;
    private final ProblemRepository problemRepository;

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
    @Transactional
    public void setUpMatch(UUID matchId, DraftSession draftSession) {
        assert matchId.equals(draftSession.getId());
        Match match = matchRepository.findById(matchId).get();
        match.setOptionsOfDraftSession(draftSession);
        var problem = pickProblemOfOptions(match.getOptions());
        log.info("Pick problem: {}", problem.getId());
        match.setProblem(problem);
        match.setStatus(Match.Status.LIVE);
        matchRepository.save(match);
    }

    public Problem pickProblemOfOptions(Map<Category, String> options) {
        if (options.get(Category.Language).equals(Language.SQL.name())) {
            throw new RuntimeException("No SQL support!");
        }

        return problemRepository.getRandomProblem(options.get(Category.ProblemType), options.get(Category.ProblemLevel));
    }

    public Match getMatchById(UUID matchId) {
        return matchRepository.getReferenceById(matchId);
    }
}
