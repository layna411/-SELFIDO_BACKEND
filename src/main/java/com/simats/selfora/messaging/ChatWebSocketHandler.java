package com.simats.selfora.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.MessageDtos;
import com.simats.selfora.entity.User;
import com.simats.selfora.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ChatSessionRegistry chatSessionRegistry;

    @Autowired
    private MessagingService messagingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            chatSessionRegistry.register(userId, session);
            Map<String, Object> welcome = new HashMap<>();
            welcome.put("type", "CONNECTED");
            welcome.put("userId", userId);
            chatSessionRegistry.sendToUser(userId, welcome);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = extractUserId(session);
        if (userId == null) return;

        try {
            JsonNode root = objectMapper.readTree(message.getPayload());
            String type = root.has("type") ? root.get("type").asText("") : "";

            switch (type) {
                case "SEND_MESSAGE" -> {
                    Long childId = root.has("childId") && !root.get("childId").isNull() ? root.get("childId").asLong() : null;
                    Long receiverId = root.has("receiverId") && !root.get("receiverId").isNull() ? root.get("receiverId").asLong() : 0L;
                    String text = root.has("messageText") ? root.get("messageText").asText("") : "";

                    if (childId != null && !text.trim().isEmpty()) {
                        MessageDtos.SendMessageRequest req = new MessageDtos.SendMessageRequest(receiverId, childId, text.trim());
                        messagingService.sendMessageAsUserId(userId, req);
                    }
                }
                case "MARK_READ" -> {
                    Long childId = root.has("childId") && !root.get("childId").isNull() ? root.get("childId").asLong() : null;
                    if (childId != null) {
                        messagingService.markConversationAsReadAsUserId(userId, childId);
                    }
                }
                case "PING" -> {
                    Map<String, Object> pong = new HashMap<>();
                    pong.put("type", "PONG");
                    chatSessionRegistry.sendToUser(userId, pong);
                }
                default -> {}
            }
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "ERROR");
            err.put("message", e.getMessage() != null ? e.getMessage() : "Invalid WebSocket payload");
            chatSessionRegistry.sendToUser(userId, err);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            chatSessionRegistry.unregister(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = extractUserId(session);
        if (userId != null) {
            chatSessionRegistry.unregister(userId, session);
        }
    }

    private Long extractUserId(WebSocketSession session) {
        Object attr = session.getAttributes().get("userId");
        if (attr instanceof Number) {
            return ((Number) attr).longValue();
        }
        return null;
    }
}

