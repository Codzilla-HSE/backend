package com.codzilla.backend.Submissions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;


@Slf4j
@Component
public class S3Initialization {

    @Autowired
    SubmissionSettings settings;

    @Autowired
    S3Client s3Client;

    @EventListener(ApplicationReadyEvent.class)
    public void initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(settings.s3().bucketName()).build());
            log.info("Already have bucket: " + settings.s3().bucketName());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(settings.s3().bucketName()).build());
            log.info("Create bucket: " + settings.s3().bucketName());
        }
    }
}
