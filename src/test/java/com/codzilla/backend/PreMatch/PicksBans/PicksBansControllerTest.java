package com.codzilla.backend.PreMatch.PicksBans;

import com.codzilla.backend.BaseIntegrationTest;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.DraftSessionResponseDTO;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.PreMatch.model.OptionEntity;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.boot.resttestclient.TestRestTemplate;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PicksBansControllerTest {
    private static final Logger log = LoggerFactory.getLogger(PicksBansControllerTest.class);
    @LocalServerPort
    private static int port;

    private static WebSocketStompClient stompClient;

    private static RestTestClient restTestClient;

    private static String user1JwtToken;
    private static String user2JwtToken;

    @Autowired
    private static ResourceLoader resourceLoader;

    @BeforeAll
    public static void setup() {

        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());


        restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:"+port).build();
        user1JwtToken = restTestClient.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", "u@gmail.co", "rawPassword", "0")).exchange()
                .expectStatus().isOk()
                .returnResult().getResponseCookies().getFirst("jwt").getValue();
    }

    @Test
    public void testBanOption() throws Exception {
        String url = "ws://localhost:" + port + "/game-ws";
        String lobbyId = "bfe4b2e0-d1bf-40c1-b0b2-d7836ff26c4b";

        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();

        httpHeaders.add(
                "Cookie",
                "jwt=" + user1JwtToken
        );

        CompletableFuture<StompSession> completableFuture = stompClient.connectAsync(
                url,
                httpHeaders,
                new StompHeaders(),
                new StompSessionHandlerAdapter() {
                }
        );

        StompSession session = completableFuture.get(
                5,
                TimeUnit.SECONDS
        );

        OptionEntity request = new OptionEntity(
                Category.Language,
                Language.CPP.name()
        );

        BlockingQueue<DraftSessionResponseDTO> queue = new LinkedBlockingQueue<>();
        session.subscribe(
                "/topic/draft-session/" + lobbyId,
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DraftSessionResponseDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        queue.add((DraftSessionResponseDTO) payload);
                    }
                }
        );

        session.send(
                "/app/" + lobbyId + "/ban",
                request
        );


        while (true) {
            var res = queue.take();
            if (res.getStatus() == DraftSessionResponseDTO.Status.ERROR) {
                log.info("Error: {}", res.getError());
            } else {
                log.info("Result: {}", res.getDraftSession().getRemainOptions());
            }

        }
//        assertTrue(session.isConnected());
    }
}