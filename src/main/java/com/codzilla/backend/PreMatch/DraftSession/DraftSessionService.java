package com.codzilla.backend.PreMatch.DraftSession;

import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.events.DraftSessionFinishedEvent;
import com.codzilla.backend.PreMatch.events.DraftSessionUpdatedEvent;
import com.codzilla.backend.PreMatch.exceptions.DraftSessionException;
import com.codzilla.backend.PreMatch.model.*;
import com.codzilla.backend.User.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class DraftSessionService {

    public DraftSessionService(DraftSessionRepository draftSessionRepository,
                               @Qualifier("threadPoolTaskScheduler") TaskScheduler taskScheduler,
                               TransactionTemplate transactionTemplate,
                               MatchSettings matchSettings,
                               ApplicationEventPublisher eventPublisher,
                               @Qualifier("categorySequence") Map<String, Category> categorySequence
    ) {
        this.draftSessionRepository = draftSessionRepository;
        this.taskScheduler = taskScheduler;
        this.transactionTemplate = transactionTemplate;
        this.matchSettings = matchSettings;
        this.eventPublisher = eventPublisher;
        this.categorySequence = categorySequence;
    }

    private final Map<String, Category> categorySequence;
    private final DraftSessionRepository draftSessionRepository;
    private final TaskScheduler taskScheduler;
    private final TransactionTemplate transactionTemplate;
    private final MatchSettings matchSettings;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Transactional
    public DraftSession processBan(User user, UUID draftSessionId, OptionEntity optionEntity) {
        var draftSession = draftSessionRepository.findByIdWithLock(draftSessionId).orElseThrow(
                () -> new RuntimeException(
                        "There is no lobby: " + draftSessionId));

        var nowUserMoving =
                draftSession.isFirstUserMove ? draftSession.getFirstUserId() :
                        draftSession.getSecondUserId();

        if (!nowUserMoving.equals(user.getId())) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.NOT_YOUR_TURN);
        }

        draftSession.banOption(
                optionEntity.getCategory(),
                optionEntity.getBanObject(),
                categorySequence
        );
        cancelTimer(draftSession.getId());

        if (draftSession.getStatus() == DraftSession.Status.FINISHED) {
            eventPublisher.publishEvent(new DraftSessionFinishedEvent(draftSession));
            draftSessionRepository.deleteById(draftSessionId);
        } else {
            eventPublisher.publishEvent(new DraftSessionUpdatedEvent(draftSession));
            startTimer(draftSessionId);
            draftSessionRepository.save(draftSession);
        }

        return draftSession;
    }

    private void startTimer(UUID draftSessionId) {

        ScheduledFuture<?> task = taskScheduler.schedule(
                () -> {
                    try {
                        transactionTemplate.executeWithoutResult(status -> {
                            executeRandomBan(draftSessionId);
                        });
                    } catch (Exception e) {
                        log.error(
                                "Ошибка при автоматическом бане для сессии {}",
                                draftSessionId,
                                e
                        );
                    }
                },
                Instant.now().plus(matchSettings.getTimeToPick())
        );
        scheduledTasks.put(
                draftSessionId,
                task
        );
    }

    private void cancelTimer(UUID draftSessionId) {
        ScheduledFuture<?> task = scheduledTasks.remove(draftSessionId);
        if (task != null) {
            task.cancel(false);
        }
    }

    public void executeRandomBan(UUID draftSessionId) {

        DraftSession draftSession =
                draftSessionRepository.findByIdWithLock(draftSessionId).orElseThrow();

        if (scheduledTasks.get(draftSessionId) == null) {
            return;
        }

        draftSession.makeRandomBan(categorySequence);
        cancelTimer(draftSession.getId());

        if (draftSession.getStatus() == DraftSession.Status.FINISHED) {
            eventPublisher.publishEvent(new DraftSessionFinishedEvent(draftSession));
            draftSessionRepository.deleteById(draftSessionId);
        } else {
            eventPublisher.publishEvent(new DraftSessionUpdatedEvent(draftSession));
            startTimer(draftSessionId);
            draftSessionRepository.save(draftSession);
        }
    }

    public Optional<DraftSession> findById(UUID id) {
        return draftSessionRepository.findById(id);
    }

    public DraftSession startDraftSession(UUID matchId, UUID firstUserId, UUID secondUserId) {
        DraftSession draftSession = new DraftSession(
                matchId,
                firstUserId,
                secondUserId,
                Category.Language
        );
        draftSessionRepository.save(draftSession);
        startTimer(draftSession.getId());
        return draftSession;
    }
}
