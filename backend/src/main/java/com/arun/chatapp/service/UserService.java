package com.arun.chatapp.service;

import com.arun.chatapp.entity.User;
import com.arun.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Your existing methods stay as they are
    public User createUser(User user) {
        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // NEW - Password Reset Methods
    public void updateUserPassword(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Encrypt the new password before saving
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    public boolean isUserVerified(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.isPresent() && userOpt.get().getIsEmailVerified();
    }

    public boolean isValidUser(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.isPresent();
    }

    // NEW - Get user for password reset validation
    public User getUserForPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getIsEmailVerified()) {
            return userOpt.get();
        }
        throw new RuntimeException("User not found or not verified: " + email);
    }
}
