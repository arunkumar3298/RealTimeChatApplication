package com.arun.chatapp.controller;

import com.arun.chatapp.service.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendshipController {

    @Autowired
    private FriendshipService friendshipService;

    // Send friend request
    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, Object> request) {
        try {
            Long requesterId = Long.valueOf(request.get("requesterId").toString());
            Long addresseeId = Long.valueOf(request.get("addresseeId").toString());

            Map<String, Object> result = friendshipService.sendFriendRequest(requesterId, addresseeId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Accept friend request
    @PostMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long userId) {
        try {
            friendshipService.acceptFriendRequest(friendshipId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Friend request accepted"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Reject friend request
    @PostMapping("/reject/{friendshipId}")
    public ResponseEntity<?> rejectFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long userId) {
        try {
            friendshipService.rejectFriendRequest(friendshipId, userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Friend request rejected"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Get pending friend requests
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingFriendRequests(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> requests = friendshipService.getPendingFriendRequests(userId);
            return ResponseEntity.ok(Map.of(
                    "requests", requests,
                    "total", requests.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Get friends list
    @GetMapping("/list")
    public ResponseEntity<?> getFriends(@RequestParam Long userId) {
        try {
            List<Map<String, Object>> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(Map.of(
                    "friends", friends,
                    "total", friends.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // Check friendship status
    @GetMapping("/status")
    public ResponseEntity<?> getFriendshipStatus(
            @RequestParam Long userId,
            @RequestParam Long otherUserId) {
        try {
            String status = friendshipService.getFriendshipStatus(userId, otherUserId);
            return ResponseEntity.ok(Map.of(
                    "status", status
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
