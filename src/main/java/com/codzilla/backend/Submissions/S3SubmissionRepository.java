package com.codzilla.backend.Submissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Optional;


@Repository
public class S3SubmissionRepository implements SubmissionRepository {
    @Autowired
    S3Client s3Client;

    @Autowired
    SubmissionSettings settings;

    @Override
    public void save(Submission submission) {
        s3Client.putObject(
                PutObjectRequest.builder()
                                .contentType("text/plain")
                                .bucket(settings.s3().bucketName())
                                .key("submissions/" + submission.id() + ".cpp")
                                .build(),
                RequestBody.fromString(submission.code())
        );
    }

    @Override
    public Optional<Submission> get(String id) {
        try {
            String code = s3Client.getObject(
                    GetObjectRequest.builder()
                                    .bucket(settings.s3().bucketName())
                                    .key("submissions/" + id + ".cpp")
                                    .build(),
                    ResponseTransformer.toBytes()

            ).asUtf8String();
            Submission result = new Submission(id, code, "");
            return Optional.of(result);
        } catch (NoSuchKeyException ex) {
            return Optional.empty();
        }

    }
}
