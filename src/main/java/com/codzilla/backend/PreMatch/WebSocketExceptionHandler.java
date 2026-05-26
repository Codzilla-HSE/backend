package com.codzilla.backend.PreMatch;

import com.codzilla.backend.PreMatch.DTO.ErrorDTO;
import com.codzilla.backend.PreMatch.exceptions.DraftSessionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Component;
import com.codzilla.backend.PreMatch.exceptions.MatchException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Component
@Slf4j
public class WebSocketExceptionHandler {
    @MessageExceptionHandler(DraftSessionException.class)
    @SendToUser("/queue/errors")
    public ErrorDTO handleDraftException(DraftSessionException ex) {
        log.warn(
                "Draft error {}",
                ex.type
        );

        return new ErrorDTO(
                ErrorDTO.ErrorStage.DRAFT_ERROR,
                ex.type.name()
        );

    }

    @MessageExceptionHandler(MatchException.class)
    @SendToUser("/queue/errors")
    public ErrorDTO handleMatchException(MatchException ex) {
        log.warn(
                "Match error: {}",
                ex.type
        );
        return new ErrorDTO(
                ErrorDTO.ErrorStage.MATCH_ERROR,
                ex.type.name()
        );
    }
}
