package com.codzilla.backend.Submissions;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "app.submission")
public record SubmissionSettings (S3 s3) {
    public record S3(
            String endpoint,
            String accessKey,
            String secretKey,
            String region,
            String bucketName
    ) {}
}

