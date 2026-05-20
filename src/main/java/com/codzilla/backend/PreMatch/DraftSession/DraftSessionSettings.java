package com.codzilla.backend.PreMatch.DraftSession;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.draft-session")
@Getter
@Setter
public class DraftSessionSettings {
    Duration timeToPick;
}
