package com.arun.chatapp.controller;

import com.arun.chatapp.entity.Message;
import com.arun.chatapp.entity.User;
import com.arun.chatapp.repository.MessageRepository;
import com.arun.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Send message
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> request) {
        try {
            String senderEmail = request.get("senderEmail");
            String receiverEmail = request.get("receiverEmail");
            String content = request.get("content");

            if (senderEmail == null || receiverEmail == null || content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            if (!userRepository.existsByEmail(senderEmail) || !userRepository.existsByEmail(receiverEmail)) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Message message = new Message(senderEmail, receiverEmail, content.trim());
            Message savedMessage = messageRepository.save(message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message sent successfully");
            response.put("messageId", savedMessage.getId());
            response.put("sentAt", savedMessage.getSentAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send message: " + e.getMessage());
        }
    }

    // Get chat history
    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(@RequestParam String email1, @RequestParam String email2) {
        try {
            List<Message> messages = messageRepository.findChatHistory(email1, email2);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages);
            response.put("total", messages.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get chat history: " + e.getMessage());
        }
    }

    // Search users
    @GetMapping("/search-users")
    public ResponseEntity<?> searchUsers(@RequestParam String query) {
        try {
            if (query == null || query.trim().length() < 2) {
                return ResponseEntity.badRequest().body("Query must be at least 2 characters");
            }

            List<User> users = userRepository.findByUsernameContainingIgnoreCase(query.trim());

            List<Map<String, Object>> userList = users.stream()
                    .filter(user -> user.getIsEmailVerified())
                    .map(user -> {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", user.getUsername());
                        userMap.put("email", user.getEmail());
                        userMap.put("id", user.getId());
                        return userMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            response.put("total", userList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Search failed: " + e.getMessage());
        }
    }

    // Get contacts
    @GetMapping("/contacts")
    public ResponseEntity<?> getChatContacts(@RequestParam String email) {
        try {
            List<String> partnerEmails = messageRepository.findChatPartners(email);

            List<Map<String, Object>> contacts = partnerEmails.stream()
                    .map(partnerEmail -> {
                        User user = userRepository.findByEmail(partnerEmail).orElse(null);
                        if (user != null) {
                            Map<String, Object> contact = new HashMap<>();
                            contact.put("username", user.getUsername());
                            contact.put("email", user.getEmail());
                            contact.put("id", user.getId());
                            return contact;
                        }
                        return null;
                    })
                    .filter(contact -> contact != null)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("contacts", contacts);
            response.put("total", contacts.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to get contacts: " + e.getMessage());
        }
    }
}
