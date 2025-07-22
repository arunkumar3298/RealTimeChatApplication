package com.arun.chatapp.service;

import com.arun.chatapp.entity.Friendship;
import com.arun.chatapp.entity.FriendshipStatus;
import com.arun.chatapp.entity.User;
import com.arun.chatapp.repository.FriendshipRepository;
import com.arun.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    // Send friend request
    public Map<String, Object> sendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new RuntimeException("Cannot send friend request to yourself");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if friendship already exists
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requester, addressee);
        if (existing.isPresent()) {
            Friendship friendship = existing.get();
            switch (friendship.getStatus()) {
                case PENDING:
                    throw new RuntimeException("Friend request already sent");
                case ACCEPTED:
                    throw new RuntimeException("You are already friends");
                case REJECTED:
                    throw new RuntimeException("Friend request was rejected");
                case BLOCKED:
                    throw new RuntimeException("Unable to send friend request");
            }
        }

        // Create new friend request
        Friendship friendship = new Friendship(requester, addressee);
        friendshipRepository.save(friendship);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Friend request sent to " + addressee.getUsername());
        return result;
    }

    // Accept friend request
    public void acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to accept this request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new RuntimeException("Friend request is not pending");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    // Reject friend request
    public void rejectFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (!friendship.getAddressee().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to reject this request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendship.setRespondedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
    }

    // Get pending friend requests (received)
    public List<Map<String, Object>> getPendingFriendRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> requests = friendshipRepository.findByAddresseeAndStatus(user, FriendshipStatus.PENDING);

        return requests.stream().map(friendship -> {
            Map<String, Object> request = new HashMap<>();
            request.put("id", friendship.getId());
            request.put("requesterId", friendship.getRequester().getId());
            request.put("requesterName", friendship.getRequester().getUsername());
            request.put("requesterEmail", friendship.getRequester().getEmail());
            request.put("requestedAt", friendship.getRequestedAt());
            return request;
        }).collect(Collectors.toList());
    }

    // Get friends list
    public List<Map<String, Object>> getFriends(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(user);

        return friendships.stream().map(friendship -> {
            User friend = friendship.getRequester().getId().equals(userId)
                    ? friendship.getAddressee()
                    : friendship.getRequester();

            Map<String, Object> friendData = new HashMap<>();
            friendData.put("id", friend.getId());
            friendData.put("username", friend.getUsername());
            friendData.put("email", friend.getEmail());
            friendData.put("isOnline", friend.getIsOnline());
            friendData.put("lastSeen", friend.getLastSeen());
            return friendData;
        }).collect(Collectors.toList());
    }

    // Check friendship status
    public String getFriendshipStatus(Long userId, Long otherUserId) {
        User user = userRepository.findById(userId).orElse(null);
        User otherUser = userRepository.findById(otherUserId).orElse(null);

        if (user == null || otherUser == null) {
            return "NOT_FOUND";
        }

        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(user, otherUser);

        if (friendship.isEmpty()) {
            return "NONE";
        }

        return friendship.get().getStatus().toString();
    }
}
