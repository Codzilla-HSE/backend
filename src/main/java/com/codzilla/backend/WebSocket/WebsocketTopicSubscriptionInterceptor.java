package com.codzilla.backend.WebSocket;

import com.codzilla.backend.MatchRoom.MatchRepository;
import com.codzilla.backend.User.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class WebsocketTopicSubscriptionInterceptor implements ChannelInterceptor {

    private final MatchRepository matchRepository;
    private final UserService userService;

    public WebsocketTopicSubscriptionInterceptor(MatchRepository matchRepository,
                                                 UserService userService) {
        this.matchRepository = matchRepository;
        this.userService = userService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            var user = accessor.getUser();
            if (destination != null && user != null) {

                if (!hasAccessToTopic(user.getName(), destination)) {

                    throw new AccessDeniedException("Вы не являетесь участником этого матча!");
                }
            }
        }
        return message;
    }

    private boolean hasAccessToTopic(String userEmail, String destination) {
        if (destination.startsWith("/topic/match/")) {
            String matchId = destination.substring("/topic/match/".length());
            var matchOpt = matchRepository.findById(UUID.fromString(matchId));
            var user = userService.getByEmail(userEmail);
            return matchOpt.filter(match -> match.getFirstUserId().equals(user.getId()) ||
                    match.getSecondUserId().equals(user.getId())).isPresent();

        }
        return true;
    }
}