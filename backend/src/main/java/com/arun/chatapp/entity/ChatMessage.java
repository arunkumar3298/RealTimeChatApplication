package com.arun.chatapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "message_status")
    private MessageStatus status; // SENT, DELIVERED, READ

    @Column(name = "message_type")
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE

    // Constructors, getters, and setters
}

enum MessageStatus {
    SENT,
    DELIVERED,
    READ
}
