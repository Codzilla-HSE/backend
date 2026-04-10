package com.codzilla.backend.controller.Sandbox.polygon;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;

import java.util.Base64;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;




@Slf4j
@Component
public class PolygonClient {

    private static final String BASE_URL = "https://polygon.codeforces.com/api/";

    @Value("${polygon.api.key}")
    private String apiKey;

    @Value("${polygon.api.secret}")
    private String apiSecret;

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PolygonClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }


    public PolygonProblem getProblemTests(String problemId) {
        var params = new TreeMap<String, String>();
        params.put("problemId", problemId);
        params.put("testset", "tests");

        String url = buildSignedUrl("problem.tests", params);

        String raw = restClient.get()
                .uri(url)
                .retrieve()
                .onStatus(status -> true, (req, res) -> {})
                .body(String.class);

        log.info("getProblemTests response: {}", raw);

        try {
            PolygonProblem result = objectMapper.readValue(raw, PolygonProblem.class);

            if ("OK".equals(result.getStatus()) && result.getResult() != null) {
                for (PolygonProblem.Test test : result.getResult()) {
                    if (test.getInputBase64() != null && !test.getInputBase64().isEmpty()) {
                        test.setInput(new String(Base64.getDecoder().decode(test.getInputBase64())));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse tests response: " + raw, e);
        }
    }


    private String buildSignedUrl(String method, TreeMap<String, String> params) {
        String rand = String.format("%06x", new SecureRandom().nextInt(0xFFFFFF));

        params.put("apiKey", apiKey);
        params.put("time", String.valueOf(Instant.now().getEpochSecond()));

        String paramStr = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        String dataToSign = rand + "/" + method + "?" + paramStr + "#" + apiSecret;
        log.info("toSign: {}", dataToSign);

        String hash = sha512(dataToSign);
        params.put("apiSig", rand + hash);


        String query = params.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8)
                        + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return method + "?" + query;
    }



    private String sha512(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] bytes = md.digest(data.getBytes("UTF-8")); // явно UTF-8
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign request", e);
        }
    }

    public String createProblem(String name) {
        var params = new TreeMap<String, String>();
        params.put("name", name);

        String url = buildSignedUrl("problem.create", params);


        String raw = restClient.post()
                .uri(url)
                .retrieve()
                .onStatus(status -> true, (req, res) -> {}) // не бросать исключение на 4xx
                .body(String.class);

        log.info("Polygon response: {}", raw);

        try {
            PolygonResponse response = objectMapper.readValue(raw, PolygonResponse.class);
            if (!"OK".equals(response.getStatus())) {
                throw new RuntimeException("Polygon error: " + response.getComment());
            }
            return response.getResult().getId().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Polygon response: " + raw, e);
        }
    }



    public void saveTest(String problemId, int index, String input, String output) {
        var params = new TreeMap<String, String>();
        params.put("problemId", problemId);
        params.put("testset", "tests");
        params.put("testIndex", String.valueOf(index));
        params.put("testInput", input);
        params.put("testOutput", output);
        params.put("testUseInStatements", "false");

        String url = buildSignedUrl("problem.saveTest", params);

        String raw = restClient.post()
                .uri(url)
                .retrieve()
                .onStatus(status -> true, (req, res) -> {})
                .body(String.class);

        log.info("saveTest response: {}", raw);

    }


}