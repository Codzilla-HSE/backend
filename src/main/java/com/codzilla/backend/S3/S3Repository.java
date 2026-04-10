package com.codzilla.backend.S3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;
import java.util.UUID;


@Repository
public class S3Repository {
    @Autowired
    S3Client s3Client;

    @Autowired
    S3Settings settings;

    public void save(byte[] content, String path) {
        s3Client.putObject(
                PutObjectRequest.builder()
                                .contentType("text/plain")
                                .bucket(settings.bucketName())
                                .key(path)
                                .build(),
                RequestBody.fromBytes(content)
        );
    }


    public Optional<byte[]> get(String path) {
        try {
            byte[] fileContent = s3Client.getObject(
                    GetObjectRequest.builder()
                                    .bucket(settings.bucketName())
                                    .key(path)
                                    .build(),
                    ResponseTransformer.toBytes()

            ).asByteArray();
            return Optional.of(fileContent);
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        }

    }
}
