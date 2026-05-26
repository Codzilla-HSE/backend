package com.codzilla.backend.PreMatch.DraftSession;

import com.codzilla.backend.PreMatch.DTO.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
                               ApplicationEventPublisher eventPublisher
    ) {
        this.draftSessionRepository = draftSessionRepository;
        this.taskScheduler = taskScheduler;
        this.transactionTemplate = transactionTemplate;
        this.matchSettings = matchSettings;
        this.eventPublisher = eventPublisher;
    }


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

        banOption(
                draftSession,
                optionEntity
        );

        if (draftSession.getStatus().equals(DraftSession.Status.PICKING)) {
            startTimer(draftSessionId);
            draftSessionRepository.save(draftSession);
        } else if (draftSession.getStatus().equals(DraftSession.Status.FINISHED)) {
            draftSessionRepository.deleteById(draftSessionId);
        }

        return draftSession;
    }

    private void banOption(DraftSession draftSession, OptionEntity optionEntity) {


        boolean isOptionExists =
                Arrays.stream(optionEntity.getCategory().getEnumClass().getEnumConstants())
                      .anyMatch(
                              anEnum -> anEnum.name().equals(optionEntity.getBanObject())
                      );


        if (!isOptionExists) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.OPTION_DO_NOT_EXISTS);
        }

        long optionsOfCategoryRemain =
                draftSession.remainOptions.get(optionEntity.getCategory()).size();

        if (optionsOfCategoryRemain == 1) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.CAN_NOT_BAN_LAST_OPTION);
        }

        if (!draftSession.remainOptions.get(optionEntity.getCategory())
                                       .contains(optionEntity.getBanObject())) {
            throw new DraftSessionException(DraftSessionException.DraftErrorType.OPTION_ALREADY_BANNED);
        }
        cancelTimer(draftSession.getId());
        draftSession.remainOptions.get(optionEntity.getCategory())
                                  .remove(optionEntity.getBanObject());
        log.info(
                "BAN OPTION: {}",
                optionEntity
        );
        draftSession.setFirstUserMove(!draftSession.isFirstUserMove());

        boolean isDraftSessionFinished =
                draftSession.remainOptions.values().stream().allMatch(
                        set -> set.size() == 1
                );

        if (isDraftSessionFinished) {
            draftSession.setStatus(DraftSession.Status.FINISHED);
            eventPublisher.publishEvent(new DraftSessionFinishedEvent(draftSession));
        } else {
            eventPublisher.publishEvent(new DraftSessionUpdatedEvent(draftSession));
        }
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

        List<OptionEntity> allOptions = draftSession.remainOptions.entrySet().stream()
                                                                  .flatMap(entry -> entry.getValue()
                                                                                         .stream()
                                                                                         .map(value -> new OptionEntity(
                                                                                                 entry.getKey(),
                                                                                                 value
                                                                                         )))
                                                                  .toList();

        while (true) {
            int randomIndex = ThreadLocalRandom.current().nextInt(allOptions.size());
            try {
                banOption(
                        draftSession,
                        allOptions.get(randomIndex)
                );
                break;
            } catch (RuntimeException _) {
            }
        }


        draftSessionRepository.save(draftSession);
        if (draftSession.getStatus().equals(DraftSession.Status.PICKING)) {
            startTimer(draftSessionId);
        }
    }

    public Optional<DraftSession> findById(UUID id) {
        return draftSessionRepository.findById(id);
    }

    public DraftSession startDraftSession(UUID matchId, UUID firstUserId, UUID secondUserId) {
        DraftSession draftSession = new DraftSession(
                matchId,
                firstUserId,
                secondUserId
        );
        draftSessionRepository.save(draftSession);
        startTimer(draftSession.getId());
        return draftSession;
    }
}
