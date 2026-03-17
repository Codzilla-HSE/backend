package com.codzilla.backend.Submissions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

import java.net.URI;


@Slf4j
@Configuration
@EnableConfigurationProperties(SubmissionSettings.class)
public class SubmissionConfiguration {

    @Autowired
    SubmissionSettings settings;

    @Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        var redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer());

        JacksonJsonRedisSerializer<Object> serializer = new JacksonJsonRedisSerializer<>(Object.class);

        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }


    @Bean
    public S3Client s3Client() {
        log.info(settings.toString());
        return S3Client.builder()
                       .endpointOverride(URI.create(settings.s3().endpoint()))
                       .credentialsProvider(StaticCredentialsProvider.create(
                               AwsBasicCredentials.create(
                                       settings.s3().accessKey(),
                                       settings.s3().secretKey())))
                       .region(Region.of(settings.s3().region()))
                       .forcePathStyle(true)
                       .build();
    }


}
