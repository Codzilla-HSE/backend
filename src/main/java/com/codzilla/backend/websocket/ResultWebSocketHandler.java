package com.codzilla.backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ResultWebSocketHandler extends TextWebSocketHandler {


    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {

        String submissionId = getSubmissionId(session);
        if (submissionId != null) {
            sessions.put(submissionId, session);
            log.info("WebSocket connected: {}", submissionId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String submissionId = getSubmissionId(session);
        if (submissionId != null) {
            sessions.remove(submissionId);
            log.info("WebSocket disconnected: {}", submissionId);
        }
    }

    public void sendResult(String submissionId, String result) {
        WebSocketSession session = sessions.get(submissionId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(result));
                sessions.remove(submissionId);
            } catch (Exception e) {
                log.error("Failed to send WebSocket message: {}", e.getMessage());
            }
        }
    }

    private String getSubmissionId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.startsWith("submissionId=")) {
            return query.substring("submissionId=".length());
        }
        return null;
    }
}