package com.codzilla.backend.S3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;


@Slf4j
@Configuration
@EnableConfigurationProperties(S3Settings.class)
public class S3Configuration {

    @Autowired
    S3Settings settings;

    @Bean
    public S3Client s3Client() {
        log.info(settings.toString());
        return S3Client.builder()
                       .endpointOverride(URI.create(settings.endpoint()))
                       .credentialsProvider(StaticCredentialsProvider.create(
                               AwsBasicCredentials.create(
                                       settings.accessKey(),
                                       settings.secretKey())))
                       .region(Region.of(settings.region()))
                       .forcePathStyle(true)
                       .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                          .region(Region.of(settings.region()))
                          .endpointOverride(URI.create(settings.endpoint()))
                          .credentialsProvider(StaticCredentialsProvider.create(
                                  AwsBasicCredentials.create(settings.accessKey(), settings.secretKey())
                          ))
                          .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                                                                                                  .pathStyleAccessEnabled(true)
                                                                                                  .build())
                          .build();
    }


}
