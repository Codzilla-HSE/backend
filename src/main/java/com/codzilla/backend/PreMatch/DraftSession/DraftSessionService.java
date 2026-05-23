package com.codzilla.backend.PreMatch.DraftSession;

import com.codzilla.backend.PreMatch.model.*;
import com.codzilla.backend.User.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
                               SimpMessagingTemplate messagingTemplate,
                               TransactionTemplate transactionTemplate,
                               DraftSessionSettings settings
    ) {
        this.draftSessionRepository = draftSessionRepository;
        this.taskScheduler = taskScheduler;
        this.messagingTemplate = messagingTemplate;
        this.transactionTemplate = transactionTemplate;
        this.settings = settings;
    }


    private final DraftSessionRepository draftSessionRepository;
    private final TaskScheduler taskScheduler;
    private final SimpMessagingTemplate messagingTemplate;
    private final TransactionTemplate transactionTemplate;
    private final DraftSessionSettings settings;

    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Transactional
    public DraftSession processBan(User user, UUID draftSessionId, OptionEntity optionEntity) {
        var draftSession = draftSessionRepository.findByIdWithLock(draftSessionId).orElseThrow(
                () -> new RuntimeException(
                        "There is no lobby: " + draftSessionId));

        cancelTimer(draftSessionId);

        var nowUserMoving =
                draftSession.isFirstUserMove ? draftSession.getFirstUserId() :
                        draftSession.getSecondUserId();

        if (!nowUserMoving.equals(user.getId())) {
            throw new DraftSessionException("It is not your turn now!");
        }

        banOption(
                draftSession,
                optionEntity
        );

        if (draftSession.getStatus().equals(DraftSession.Status.PICKING)) {
            startTimer(draftSessionId);
        }

        draftSessionRepository.save(draftSession);

        return draftSession;
    }

    private void banOption(DraftSession draftSession, OptionEntity optionEntity) {


        boolean isOptionExists =
                Arrays.stream(optionEntity.getCategory().getEnumClass().getEnumConstants())
                      .anyMatch(
                              anEnum -> anEnum.name().equals(optionEntity.getBanObject())
                      );


        if (!isOptionExists) {
            throw new DraftSessionException("Invalid ban option!");
        }

        long optionsOfCategoryRemain =
                draftSession.remainOptions.get(optionEntity.getCategory()).size();

        if (optionsOfCategoryRemain == 1) {
            throw new DraftSessionException("There is only one object in this category! You cant " +
                                                    "ban it.");
        }

        if (!draftSession.remainOptions.get(optionEntity.getCategory())
                                       .contains(optionEntity.getBanObject())) {
            throw new DraftSessionException("Already banned: " + optionEntity.getBanObject());
        }

        draftSession.remainOptions.get(optionEntity.getCategory())
                                  .remove(optionEntity.getBanObject());
        draftSession.setFirstUserMove(!draftSession.isFirstUserMove());

        boolean isDraftSessionFinished =
                draftSession.remainOptions.values().stream().allMatch(
                        set -> set.size() == 1
                );

        if (isDraftSessionFinished) {
            draftSession.setStatus(DraftSession.Status.FINISHED);
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
                Instant.now().plus(settings.getTimeToPick())
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

    @Transactional
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
                log.info("BANNED");
                break;
            } catch (RuntimeException _) {
            }
        }


        draftSessionRepository.save(draftSession);

        messagingTemplate.convertAndSend(
                "/topic/draft-session/" + draftSessionId,
                new DraftSessionResponseDTO(
                        DraftSessionResponseDTO.Status.SUCCEED,
                        draftSession,
                        null
                )
        );
        if (draftSession.getStatus().equals(DraftSession.Status.PICKING)) {
            startTimer(draftSessionId);
        }
    }

    public Optional<DraftSession> findById(UUID id) {
        return draftSessionRepository.findById(id);
    }
    public DraftSession startDraftSession(UUID firstUserId, UUID secondUserId) {
        DraftSession draftSession = new DraftSession(firstUserId, secondUserId);
        draftSessionRepository.save(draftSession);
        startTimer(draftSession.getId());
        return draftSession;
    }
}
