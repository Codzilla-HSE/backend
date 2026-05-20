package com.codzilla.backend.PreMatch.PicksBans;

import com.codzilla.backend.PreMatch.DraftSession.DraftSession;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionRepository;
import com.codzilla.backend.PreMatch.DraftSession.DraftSessionService;
import com.codzilla.backend.PreMatch.model.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.model.OptionEntity;
import com.codzilla.backend.PreMatch.model.DraftSessionException;
import com.codzilla.backend.User.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@Controller
public class PicksBansController {
    private final DraftSessionRepository draftSessionRepository;

    public PicksBansController(SimpMessagingTemplate messagingTemplate,
                               DraftSessionService draftSessionService,
                               DraftSessionRepository draftSessionRepository) {
        this.draftSessionService = draftSessionService;
        this.messagingTemplate = messagingTemplate;
        this.draftSessionRepository = draftSessionRepository;
    }

    DraftSessionService draftSessionService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("{draftSessionId}/ban")
    void handleBanRequest(
            @DestinationVariable String draftSessionId,
            @AuthenticationPrincipal Authentication auth,
            @Payload OptionEntity optionEntity) {
        User user = (User) auth.getPrincipal();
        log.info("In ban controller");
        log.info(
                "User : {}",
                user
        );
        log.info(
                "Request: {}",
                optionEntity.getBanObject()
        );
        log.info("All sessions: {}", draftSessionRepository.findAll().stream().map(DraftSession::getId).toList());
        try {
            assert user != null;
            var draftSession = draftSessionService.processBan(
                    user,
                    UUID.fromString(draftSessionId),
                    optionEntity
            );
            messagingTemplate.convertAndSend(
                    "/topic/draft-session/" + draftSessionId,
                    new DraftSessionResponseDTO(
                            DraftSessionResponseDTO.Status.SUCCEED,
                            draftSession,
                            null
                    )
            );
        } catch (DraftSessionException exception) {
            log.info(
                    "Exception: {}",
                    exception.toString()
            );
            messagingTemplate.convertAndSend(
                    "/topic/draft-session/" + draftSessionId,
                    new DraftSessionResponseDTO(
                            DraftSessionResponseDTO.Status.ERROR,
                            null,
                            exception.getMessage()
                    )
            );
        }
    }
}
