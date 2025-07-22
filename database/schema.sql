-- Chat Application Database Schema
CREATE DATABASE IF NOT EXISTS chat_application;
USE chat_application;

-- Users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_email_verified BOOLEAN DEFAULT FALSE NOT NULL,
    email_verified_at TIMESTAMP NULL,
    profile_picture_url VARCHAR(500) NULL,
    bio TEXT NULL,
    is_online BOOLEAN DEFAULT FALSE NOT NULL,
    last_seen TIMESTAMP NULL,
    privacy_settings ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') DEFAULT 'PUBLIC'
);

-- OTP verification table
CREATE TABLE otp_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    otp_type ENUM('REGISTRATION', 'PASSWORD_RESET') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    attempts INT DEFAULT 0
);

-- Messages table
CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_email VARCHAR(100) NOT NULL,
    receiver_email VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    message_type ENUM('TEXT', 'IMAGE', 'FILE') DEFAULT 'TEXT',
    FOREIGN KEY (sender_email) REFERENCES users(email),
    FOREIGN KEY (receiver_email) REFERENCES users(email)
);

-- Friendships table
CREATE TABLE friendships (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    FOREIGN KEY (requester_id) REFERENCES users(id),
    FOREIGN KEY (addressee_id) REFERENCES users(id)
);
