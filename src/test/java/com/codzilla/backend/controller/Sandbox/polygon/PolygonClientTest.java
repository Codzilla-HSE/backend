package com.codzilla.backend.controller.Sandbox.polygon;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PolygonClientTest {

    @Autowired
    public PolygonClientTest(PolygonClient client) {
        this.client = client;
    }

    PolygonClient client;

    @Test
    void getProblemTests() {
        client.getProblemTests("533148");
    }
}