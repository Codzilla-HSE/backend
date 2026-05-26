package com.codzilla.backend.PreMatch.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebSocketDTO {
    public enum Status {
        MATCH_STARTED_REDIRECT,
        DRAFT,
        LIVE
    }

    public WebSocketDTO(Status status, Object payload) {
        this.status = status;
        this.payload = payload;
    }

    Status status;
    Object payload;
}
