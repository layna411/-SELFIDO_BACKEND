package com.simats.selfora.messaging;

import com.simats.selfora.dto.MessageDtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessagingController {

    @Autowired
    private MessagingService messagingService;

    @PostMapping
    public ResponseEntity<MessageDtos.MessageResponse> sendMessage(@RequestBody MessageDtos.SendMessageRequest request) {
        return ResponseEntity.ok(messagingService.sendMessage(request));
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<List<MessageDtos.MessageResponse>> getConversation(@PathVariable Long childId) {
        return ResponseEntity.ok(messagingService.getConversationForChild(childId));
    }

    @PostMapping("/child/{childId}/read")
    public ResponseEntity<Void> markConversationAsRead(@PathVariable Long childId) {
        messagingService.markConversationAsRead(childId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/partner/child/{childId}")
    public ResponseEntity<MessageDtos.ChatPartnerResponse> getChatPartner(@PathVariable Long childId) {
        return ResponseEntity.ok(messagingService.getChatPartnerForChild(childId));
    }
}
