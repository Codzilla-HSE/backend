package com.codzilla.backend.S3;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class S3RepositoryTest {

    @Autowired
    S3Repository repository;

    @Test
    void save() {
        byte[] content = "123".getBytes();

        String path = "test/123";

        repository.save(
                content,
                path
        );
    }

    @Test
    void get() {
        var res = repository.get("test/123");
        assertTrue(res.isPresent());
        assertEquals(
                "123",
                new String(res.get())
        );
    }
}