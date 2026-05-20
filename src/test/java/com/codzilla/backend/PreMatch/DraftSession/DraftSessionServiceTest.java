package com.codzilla.backend.PreMatch.DraftSession;

import com.codzilla.backend.Authentication.dto.RegisterRequestDTO;
import com.codzilla.backend.BaseIntegrationTest;
import com.codzilla.backend.PreMatch.model.*;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import com.codzilla.backend.User.UserService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.servlet.support.WebContentGenerator;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DraftSessionServiceTest extends BaseIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(DraftSessionServiceTest.class);
    @LocalServerPort
    private int port;

    @MockitoBean
    DraftSessionSettings draftSessionSettings;

    private RestTestClient restTestClient;

    private BlockingQueue<DraftSessionResponseDTO> user1Queue;
    private BlockingQueue<DraftSessionResponseDTO> user2Queue;

    private StompSession user1Session;
    private StompSession user2Session;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DraftSessionRepository draftSessionRepository;

    @Autowired
    private UserService userService;

    private User user1;
    private User user2;

    String user1Jwt;
    String user2Jwt;

    private DraftSession draftSession;
    @Autowired
    private WebContentGenerator webContentGenerator;

    @BeforeEach
    void setup() throws Exception {
        restTestClient = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();


        userService.registerUser(new RegisterRequestDTO(
                "user1",
                "email1",
                "password1"
        ));
        userService.registerUser(new RegisterRequestDTO(
                "user2",
                "email2",
                "password2"
        ));

        this.user1 = userService.getByEmail("email1");
        this.user2 = userService.getByEmail("email2");

        this.user1Jwt =
                restTestClient.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                              .body(Map.of(
                                      "email",
                                      user1.getEmail(),
                                      "rawPassword",
                                      "password1"
                              )).exchange()
                              .expectStatus().isOk()
                              .returnResult().getResponseCookies().getFirst("jwt").getValue();

        this.user2Jwt =
                restTestClient.post().uri("/auth/login").contentType(MediaType.APPLICATION_JSON)
                              .body(Map.of(
                                      "email",
                                      user2.getEmail(),
                                      "rawPassword",
                                      "password2"
                              )).exchange()
                              .expectStatus().isOk()
                              .returnResult().getResponseCookies().getFirst("jwt").getValue();
        createDraftSession();

        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));

        SockJsClient sockJsClient1 = new SockJsClient(transports);
        SockJsClient sockJsClient2 = new SockJsClient(transports);

        var stompClient1 = new WebSocketStompClient(sockJsClient1);
        var stompClient2 = new WebSocketStompClient(sockJsClient2);

        stompClient1.setMessageConverter(new JacksonJsonMessageConverter());
        stompClient2.setMessageConverter(new JacksonJsonMessageConverter());

        WebSocketHttpHeaders httpHeaders1 = new WebSocketHttpHeaders();
        WebSocketHttpHeaders httpHeaders2 = new WebSocketHttpHeaders();

        httpHeaders1.add(
                "Cookie",
                "jwt=" + user1Jwt
        );
        httpHeaders2.add(
                "Cookie",
                "jwt=" + user2Jwt
        );

        String url = "ws://localhost:" + port + "/game-ws";
        CompletableFuture<StompSession> completableFuture1 = stompClient1.connectAsync(
                url,
                httpHeaders1,
                new StompHeaders(),
                new StompSessionHandlerAdapter() {
                }
        );
        CompletableFuture<StompSession> completableFuture2 = stompClient2.connectAsync(
                url,
                httpHeaders2,
                new StompHeaders(),
                new StompSessionHandlerAdapter() {
                }
        );
        user1Session = completableFuture1.get(
                5,
                TimeUnit.SECONDS
        );
        user2Session = completableFuture2.get(
                5,
                TimeUnit.SECONDS
        );

        user1Queue = new LinkedBlockingQueue<>();
        user2Queue = new LinkedBlockingQueue<>();

        user1Session.subscribe(
                "/topic/draft-session/" + draftSession.getId(),
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DraftSessionResponseDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        user1Queue.add((DraftSessionResponseDTO) payload);
                    }
                }
        );
        user2Session.subscribe(
                "/topic/draft-session/" + draftSession.getId(),
                new StompSessionHandlerAdapter() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return DraftSessionResponseDTO.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                        user2Queue.add((DraftSessionResponseDTO) payload);
                    }
                }
        );
        when(draftSessionSettings.getTimeToPick()).thenReturn(Duration.ofMillis(500));
    }

    @AfterEach
    void tearDown() {
        draftSessionRepository.deleteAll();
        userRepository.deleteAll();

        if (user1Session != null) {
            user1Session.disconnect();
        }

        if (user2Session != null) {
            user2Session.disconnect();
        }
    }

    void createDraftSession() {
        var draftSession = new DraftSession(
                user1.getId(),
                user2.getId()
        );
        this.draftSession = draftSessionRepository.save(draftSession);
        log.info("Draft session id: " + draftSession.getId());
    }

    @Test
    void DraftSession_HappyPath() throws InterruptedException {
        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.Language,
                        Language.CPP.name()
                )
        );


        var res1 = user1Queue.take();
        var res2 = user2Queue.take();

        assertNotEquals(
                null,
                res1
        );
        assertNotEquals(
                null,
                res2
        );

        assertEquals(
                DraftSessionResponseDTO.Status.SUCCEED,
                res1.getStatus()
        );

        assertFalse(res1.getDraftSession().getRemainOptions().get(Category.Language)
                        .contains(Language.CPP.name()));

        assertFalse(res1.getDraftSession().isFirstUserMove);

        user2Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.ProblemType,
                        ProblemType.Algorithm.name()
                )
        );

        res1 = user1Queue.take();

        res2 = user2Queue.take();

        assertNotEquals(
                null,
                res1
        );
        assertNotEquals(
                null,
                res2
        );

        assertFalse(res2.getDraftSession().remainOptions.get(Category.ProblemType)
                                                        .contains(ProblemType.Algorithm.name()));
    }

    @Test
    void DraftSession_OneUserCantBanTwice() throws InterruptedException {
        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.Language,
                        Language.CPP.name()
                )
        );

        var res = user1Queue.poll(
                1,
                TimeUnit.SECONDS
        );
        assertNotNull(res);

        assertEquals(
                DraftSessionResponseDTO.Status.SUCCEED,
                res.getStatus()
        );

        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.ProblemType,
                        ProblemType.Algorithm.name()
                )
        );

        res = user1Queue.poll(
                1,
                TimeUnit.SECONDS
        );
        assertNotNull(res);

        assertEquals(
                DraftSessionResponseDTO.Status.ERROR,
                res.getStatus()
        );

        log.info("Error message: " + res.getError());
    }

    @Test
    void DraftSession_CantBanWrongOption() throws InterruptedException {
        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.Language,
                        "wrong language"
                )
        );

        var res = user1Queue.poll(
                1,
                TimeUnit.SECONDS
        );
        assertNotNull(res);
        assertEquals(
                DraftSessionResponseDTO.Status.ERROR,
                res.getStatus(),
                "We cant ban not existing option!"
        );

        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.ProblemType,
                        ProblemType.Algorithm.name()
                )
        );

        res = user1Queue.poll(
                1,
                TimeUnit.SECONDS
        );
        assertNotNull(res);

        user2Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.ProblemType,
                        ProblemType.Algorithm.name()
                )
        );

        res = user1Queue.poll(
                1,
                TimeUnit.SECONDS
        );

        assertNotNull(res);

        assertEquals(
                DraftSessionResponseDTO.Status.ERROR,
                res.getStatus(),
                "We can ban one option twice!"
        );

    }

    @Test
    void DraftSession_CantBanTheLastOptionInCategory() throws InterruptedException {

        log.info("All sessions : " +
                         draftSessionRepository.findAll().stream().map(DraftSession::getId)
                                               .toList());

        var categoryOptional = Arrays.stream(Category.values()).findFirst();
        assertTrue(
                categoryOptional.isPresent(),
                "There is no category at all!"
        );
        var category = categoryOptional.get();

        List<DraftSessionResponseDTO> allMessages = new ArrayList<>();
        for (var option : category.getEnumClass().getEnumConstants()) {
            (draftSession.isFirstUserMove() ? user1Session : user2Session)
                    .send(
                            "/app/" + draftSession.getId() + "/ban",
                            new OptionEntity(
                                    category,
                                    option.name()
                            )
                    );
            draftSession.setFirstUserMove(!draftSession.isFirstUserMove);
            log.info(
                    "{}, {}",
                    category,
                    option
            );
            allMessages.add(user1Queue.take());
        }

        assertEquals(
                DraftSessionResponseDTO.Status.ERROR,
                allMessages.getLast().getStatus()
        );

        log.info(
                "Error message: {}",
                allMessages.getLast().getError()
        );
    }

    @Test
    void DraftSession_WhenTimeToPickExceededItIsRandomBan() throws InterruptedException {
        when(draftSessionSettings.getTimeToPick()).thenReturn(Duration.ofMillis(100));
        user1Session.send(
                "/app/" + draftSession.getId() + "/ban",
                new OptionEntity(
                        Category.Language,
                        Language.CPP.name()
                )
        );

        var res = user1Queue.take();
        assertFalse(res.getDraftSession().isFirstUserMove());
        var optionsRemainAfterFirstBan = res.getDraftSession().getRemainOptions().values()
                                            .stream().map(Set::size)
                                            .reduce(
                                                    0,
                                                    Integer::sum
                                            );

        var resAfterRandomBan = user1Queue.poll(
                110,
                TimeUnit.MILLISECONDS
        );

        assertNotNull(resAfterRandomBan);
        assertNotNull(resAfterRandomBan.getDraftSession());
        assertTrue(resAfterRandomBan.getDraftSession().isFirstUserMove());

        var optionsRemainAfterRandomBan =
                resAfterRandomBan.getDraftSession().getRemainOptions().values()
                                 .stream().map(Set::size)
                                 .reduce(
                                         0,
                                         Integer::sum
                                 );

        assertEquals(
                optionsRemainAfterFirstBan - 1,
                optionsRemainAfterRandomBan
        );
    }

}