package com.arun.chatapp.repository;

import com.arun.chatapp.entity.Friendship;
import com.arun.chatapp.entity.FriendshipStatus;
import com.arun.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    // Find friendship between two users
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)")
    Optional<Friendship> findFriendshipBetween(@Param("user1") User user1, @Param("user2") User user2);

    // Find pending friend requests received by user
    List<Friendship> findByAddresseeAndStatus(User addressee, FriendshipStatus status);

    // Find pending friend requests sent by user
    List<Friendship> findByRequesterAndStatus(User requester, FriendshipStatus status);

    // Find all accepted friendships for a user
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :user OR f.addressee = :user) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("user") User user);

    // Check if users are friends
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "((f.requester = :user1 AND f.addressee = :user2) OR " +
            "(f.requester = :user2 AND f.addressee = :user1)) AND f.status = 'ACCEPTED'")
    boolean areFriends(@Param("user1") User user1, @Param("user2") User user2);
}
