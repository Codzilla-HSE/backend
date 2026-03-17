package com.codzilla.backend.Submissions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Test implements CommandLineRunner {
    private final S3SubmissionRepository s3;
    private final RedisSubmissionRepository repository;

    public Test(S3SubmissionRepository s3, RedisSubmissionRepository repository) {
        this.s3 = s3;
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        Submission submission = new Submission("1233", "codee", "email");
//        repository.save(submission);
//        var sub = repository.get(submission.id());
//        log.info("Got sub: " + sub.get());

//        s3.save(submission);

//        log.info(s3.get(submission.id()).toString());
    }
}
