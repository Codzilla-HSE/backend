package com.codzilla.backend.PreMatch.DraftSession;

import com.codzilla.backend.PreMatch.DTO.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.DTO.WebSocketDTO;
import com.codzilla.backend.PreMatch.MatchSettings;
import com.codzilla.backend.PreMatch.model.OptionEntity;
import com.codzilla.backend.PreMatch.exceptions.DraftSessionException;
import com.codzilla.backend.User.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
public class DraftSessionController {

    public DraftSessionController(SimpMessagingTemplate messagingTemplate,
                                  DraftSessionService draftSessionService,
                                  MatchSettings matchSettings) {
        this.draftSessionService = draftSessionService;
        this.messagingTemplate = messagingTemplate;
        this.matchSettings = matchSettings;
    }

    DraftSessionService draftSessionService;
    SimpMessagingTemplate messagingTemplate;
    private final MatchSettings matchSettings;

    @MessageMapping("{matchId}/ban")
    void handleBanRequest(
            @DestinationVariable String matchId,
            @AuthenticationPrincipal Authentication auth,
            @Payload OptionEntity optionEntity) {
        User user = (User) auth.getPrincipal();
        assert user != null;
        var draftSession = draftSessionService.processBan(
                user,
                UUID.fromString(matchId),
                optionEntity
        );

    }

    @SubscribeMapping("/match/{matchId}")
    public WebSocketDTO getInitialState(@DestinationVariable UUID matchId) {


        var draftSession = draftSessionService.findById(matchId);
        log.info("Subscribed.");
        if (draftSession.isEmpty()) {
            return null;
        }
        return new WebSocketDTO(
                WebSocketDTO.Status.DRAFT,
                new DraftSessionResponseDTO(
                        draftSession.get()
                )
        );

    }
}
