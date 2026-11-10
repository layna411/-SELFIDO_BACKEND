package com.simats.selfora.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simats.selfora.dto.MessageDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatSessionRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArraySet<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    public void register(Long userId, WebSocketSession session) {
        if (userId == null || session == null) return;
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(Long userId, WebSocketSession session) {
        if (userId == null || session == null) return;
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSessions.remove(userId);
            }
        }
    }

    public boolean isUserOnline(Long userId) {
        if (userId == null) return false;
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    public void sendToUser(Long userId, Map<String, Object> payload) {
        if (userId == null) return;
        CopyOnWriteArraySet<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;

        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(textMessage);
                        }
                    }
                } else {
                    sessions.remove(session);
                }
            }
        } catch (Exception ignored) {}
    }

    public void broadcastNewMessage(MessageDtos.MessageResponse msg, Collection<Long> participantUserIds) {
        if (msg == null) return;
        Set<Long> targets = new HashSet<>();
        if (msg.getSenderId() != null) targets.add(msg.getSenderId());
        if (msg.getReceiverId() != null) targets.add(msg.getReceiverId());
        if (participantUserIds != null) targets.addAll(participantUserIds);

        Map<String, Object> msgPayload = new HashMap<>();
        msgPayload.put("type", "NEW_MESSAGE");
        msgPayload.put("message", msg);

        Map<String, Object> contactsPayload = new HashMap<>();
        contactsPayload.put("type", "CONTACTS_UPDATED");
        contactsPayload.put("childId", msg.getChildId());

        for (Long uid : targets) {
            sendToUser(uid, msgPayload);
            sendToUser(uid, contactsPayload);
        }
    }

    public void broadcastMessagesRead(Long childId, Long readByUserId, Collection<Long> participantUserIds) {
        if (childId == null || participantUserIds == null) return;
        Map<String, Object> readPayload = new HashMap<>();
        readPayload.put("type", "MESSAGES_READ");
        readPayload.put("childId", childId);
        readPayload.put("readByUserId", readByUserId);

        Map<String, Object> contactsPayload = new HashMap<>();
        contactsPayload.put("type", "CONTACTS_UPDATED");
        contactsPayload.put("childId", childId);

        for (Long uid : participantUserIds) {
            if (!Objects.equals(uid, readByUserId)) {
                sendToUser(uid, readPayload);
            }
            sendToUser(uid, contactsPayload);
        }
    }
}

