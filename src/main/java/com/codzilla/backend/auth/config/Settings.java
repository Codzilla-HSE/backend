package com.codzilla.backend.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
public class Settings {
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
}
