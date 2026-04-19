package com.codzilla.backend.controller.Sandbox.judge0;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires running Judge0 instance")
class Judge0ClientTest {

    @Autowired
    Judge0Client client;

    @Test
    void testSimpleOfJudge0() throws InterruptedException {
        String token = client.submitAsync("print(1)", 71, "", "1");
        Thread.sleep(5000);
        System.out.println("STATUS: " + client.getSubmissionStatus(token).toString());
    }
}