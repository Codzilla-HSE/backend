package com.codzilla.backend.S3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.s3")
public record S3Settings(
        String endpoint,
        String accessKey,
        String secretKey,
        String region,
        String bucketName

) {
}

