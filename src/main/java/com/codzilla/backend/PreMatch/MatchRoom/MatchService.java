package com.codzilla.backend.PreMatch.MatchRoom;


import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionService;
import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.problem.Problem;
import com.codzilla.backend.judge.problem.ProblemRepository;
import com.codzilla.backend.judge.problem.ProblemService;
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
    private final SqlServiceClient sqlServiceClient;
    private final ProblemService problemService;

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
        // SQL в доменной модели — это ЯЗЫК (Category.Language == SQL), а не ProblemType.
        // Поэтому SQL-задачу определяем по выбранному языку, а не по ProblemType.
        String language = options.get(Category.Language);
        String problemType = options.get(Category.ProblemType);
        String problemLevel = options.getOrDefault(Category.ProblemLevel, "EASY").toUpperCase();
        log.info("Pick problem of options: language={}, type={}, level={}",
                language, problemType, problemLevel);

        if (language != null && language.equalsIgnoreCase(Language.SQL.name())) {
            log.info("Choosing SQL problem");
            Problem sqlProblem = problemService.getOrCreateRandomSqlProblem(problemLevel);
            log.info("Selected SQL problem: id={}, type={}", sqlProblem.getId(), sqlProblem.getType());
            return sqlProblem;
        }

        // Алгоритмическая задача: тип берём из ProblemType (ALGORITHM/MATH/DATA_STRUCTURES),
        // по умолчанию ALGORITHM.
        String algoType = (problemType != null) ? problemType : "ALGORITHM";
        Problem algoProblem = problemService.getOrCreateRandomAlgoProblem(algoType, problemLevel);
        log.info("Selected ALGO problem: id={}, type={}", algoProblem.getId(), algoProblem.getType());
        return algoProblem;
    }

    public Match getMatchById(UUID matchId) {
        return matchRepository.getReferenceById(matchId);
    }
}
