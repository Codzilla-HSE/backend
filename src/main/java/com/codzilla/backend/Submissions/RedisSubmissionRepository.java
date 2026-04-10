package com.codzilla.backend.Submissions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RedisSubmissionRepository {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;


    public void save(Submission submission) {
        redisTemplate.opsForValue().set("submission:" + submission.id(), submission);
    }


    public Optional<Submission> get(UUID id) {
        Object value = redisTemplate.opsForValue().get("submission:"+id);
        if (value == null) return Optional.empty();
        return Optional.of(objectMapper.convertValue(value, Submission.class));
    }
}
