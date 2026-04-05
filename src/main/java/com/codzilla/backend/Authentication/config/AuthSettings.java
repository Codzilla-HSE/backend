package com.codzilla.backend.Authentication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
public class AuthSettings {
    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
}
