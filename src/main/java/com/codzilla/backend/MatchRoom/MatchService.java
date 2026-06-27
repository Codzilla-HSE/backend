package com.codzilla.backend.MatchRoom;

import com.codzilla.backend.MatchRoom.dto.MatchHistoryEntryDTO;
import com.codzilla.backend.Rating.MatchFinishedEvent;
import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionService;
import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.problem.Problem;
import com.codzilla.backend.judge.problem.ProblemRepository;
import com.codzilla.backend.judge.problem.ProblemService;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
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
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

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

    @Transactional
    public boolean finishMatch(UUID matchId, UUID winnerId) {
        Match match = matchRepository.findById(matchId).orElse(null);
        if (match == null) {
            log.warn("finishMatch: match {} not found", matchId);
            return false;
        }
        if (match.getStatus() == Match.Status.FINISHED) {
            return false;
        }

        UUID loserId = match.opponentOf(winnerId);
        if (loserId == null) {
            log.warn("finishMatch: user {} is not a participant of match {}", winnerId, matchId);
            return false;
        }

        match.setStatus(Match.Status.FINISHED);
        match.setWinnerId(winnerId);
        match.setFinishedAt(Instant.now());
        matchRepository.save(match);

        eventPublisher.publishEvent(new MatchFinishedEvent(matchId, winnerId, loserId));
        log.info("Match {} finished, winner {}", matchId, winnerId);
        return true;
    }

    public Problem pickProblemOfOptions(Map<Category, String> options) {
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


        String algoType = (problemType != null) ? problemType : "ALGORITHM";
        Problem algoProblem = problemService.getOrCreateRandomAlgoProblem(algoType, problemLevel);
        log.info("Selected ALGO problem: id={}, type={}", algoProblem.getId(), algoProblem.getType());
        log.info("Pick problem of options: language={}, type={}, level={}", language, problemType, problemLevel);
        return algoProblem;
    }

    public Match getMatchById(UUID matchId) {
        return matchRepository.getReferenceById(matchId);
    }

    @Transactional(readOnly = true)
    public List<MatchHistoryEntryDTO> getMatchHistory(UUID userId) {
        return matchRepository.findFinishedByUser(userId).stream()
                .map(match -> {
                    UUID opponentId = match.opponentOf(userId);
                    String opponentNickname = userRepository.findById(opponentId)
                            .map(User::getNickname)
                            .orElse("Неизвестный соперник");
                    boolean won = userId.equals(match.getWinnerId());
                    Integer rating = userId.equals(match.getFirstUserId())
                            ? match.getFirstUserRating()
                            : match.getSecondUserRating();
                    return new MatchHistoryEntryDTO(
                            match.getId(),
                            opponentNickname,
                            won,
                            rating,
                            match.getFinishedAt()
                    );
                })
                .toList();
    }
}
