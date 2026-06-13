package com.codzilla.backend.PreMatch;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "app.match")
@Getter
@Setter
public class MatchSettings {
    Duration timeToPick;
    String websocketMatchPrefix;

    public String getWebSocketMatchDestination(UUID matchId) {
        return websocketMatchPrefix + matchId;
    }
}
