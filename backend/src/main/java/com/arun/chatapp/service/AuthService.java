package com.arun.chatapp.service;

import com.arun.chatapp.entity.User;
import com.arun.chatapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Map<String, Object> authenticateUser(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        // Find user by username
        Optional<User> userOptional = userService.getUserByUsername(username);

        if (userOptional.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found!");
            return response;
        }

        User user = userOptional.get();

        // Check password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid password!");
            return response;
        }

        // Generate JWT token
        String jwt = jwtUtil.generateJwtToken(username);

        response.put("success", true);
        response.put("message", "Login successful!");
        response.put("token", jwt);
        response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        ));

        return response;
    }
}
