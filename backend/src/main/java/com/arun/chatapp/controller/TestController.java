package com.arun.chatapp.controller;

import com.arun.chatapp.entity.User;
import com.arun.chatapp.repository.UserRepository;
import com.arun.chatapp.service.EmailService;
import com.arun.chatapp.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    // Basic welcome endpoint
    @GetMapping("/")
    public String home() {
        return "🎉 Welcome to Arun's Chat Application Backend!";
    }

    // API test endpoint
    @GetMapping("/api/test")
    public String test() {
        return "Spring Boot + Java 23.0.1 + MySQL working perfectly!";
    }

    // Create user with proper error handling
    @PostMapping("/api/test/user")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Check if username already exists
            if (userRepository.existsByUsername(user.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("Error: Username '" + user.getUsername() + "' is already taken!");
            }

            // Check if email already exists
            if (userRepository.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Error: Email '" + user.getEmail() + "' is already registered!");
            }

            // Validate required fields
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Username is required!");
            }

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Email is required!");
            }

            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: Password is required!");
            }

            // Save user if all validations pass
            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);

        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations
            String errorMessage = "Error: Database constraint violation. ";

            if (e.getMessage().contains("username")) {
                errorMessage += "Username already exists.";
            } else if (e.getMessage().contains("email")) {
                errorMessage += "Email already exists.";
            } else {
                errorMessage += "Please check your data for duplicates.";
            }

            return ResponseEntity.badRequest().body(errorMessage);
        } catch (Exception e) {
            // Handle any other unexpected errors
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to create user. Please try again.");
        }
    }

    // Get all users
    @GetMapping("/api/test/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get user by ID
    @GetMapping("/api/test/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);

            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to retrieve user with ID " + id);
        }
    }

    // Get user by username
    @GetMapping("/api/test/user/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        try {
            Optional<User> user = userRepository.findByUsername(username);

            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.notFound()
                        .build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to find user with username " + username);
        }
    }

    // Get user count
    @GetMapping("/api/test/count")
    public ResponseEntity<String> getUserCount() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok("Total users in database: " + count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to count users");
        }
    }

    // Delete user by ID (for testing purposes)
    @DeleteMapping("/api/test/user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                return ResponseEntity.ok("User with ID " + id + " deleted successfully!");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to delete user with ID " + id);
        }
    }

    // Update user (for testing purposes)
    @PutMapping("/api/test/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        try {
            Optional<User> existingUserOpt = userRepository.findById(id);

            if (!existingUserOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            User existingUser = existingUserOpt.get();

            // Check if new username is taken by another user
            if (!existingUser.getUsername().equals(updatedUser.getUsername())
                    && userRepository.existsByUsername(updatedUser.getUsername())) {
                return ResponseEntity.badRequest()
                        .body("Error: Username '" + updatedUser.getUsername() + "' is already taken!");
            }

            // Check if new email is taken by another user
            if (!existingUser.getEmail().equals(updatedUser.getEmail())
                    && userRepository.existsByEmail(updatedUser.getEmail())) {
                return ResponseEntity.badRequest()
                        .body("Error: Email '" + updatedUser.getEmail() + "' is already registered!");
            }

            // Update fields
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setPassword(updatedUser.getPassword());

            User savedUser = userRepository.save(existingUser);
            return ResponseEntity.ok(savedUser);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body("Error: Duplicate data found. Please check username and email.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to update user with ID " + id);
        }
    }

    // Check if username exists
    @GetMapping("/api/test/check/username/{username}")
    public ResponseEntity<String> checkUsernameExists(@PathVariable String username) {
        try {
            boolean exists = userRepository.existsByUsername(username);
            if (exists) {
                return ResponseEntity.ok("Username '" + username + "' already exists");
            } else {
                return ResponseEntity.ok("Username '" + username + "' is available");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to check username availability");
        }
    }

    // Check if email exists
    @GetMapping("/api/test/check/email/{email}")
    public ResponseEntity<String> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = userRepository.existsByEmail(email);
            if (exists) {
                return ResponseEntity.ok("Email '" + email + "' already exists");
            } else {
                return ResponseEntity.ok("Email '" + email + "' is available");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: Unable to check email availability");
        }
    }

    // EMAIL TESTING METHODS - NEW FUNCTIONALITY

    // Test basic email sending
    @PostMapping("/api/test/send-email")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            emailService.sendOtpEmail(email, "123456", "REGISTRATION");
            return ResponseEntity.ok("Test email sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send email: " + e.getMessage());
        }
    }

    // Test OTP generation and email sending
    @PostMapping("/api/test/generate-otp")
    public ResponseEntity<String> testOtpGeneration(@RequestParam String email) {
        try {
            String result = otpService.generateAndSendOtp(email, "REGISTRATION");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to generate OTP: " + e.getMessage());
        }
    }

    // Test OTP verification
    @PostMapping("/api/test/verify-otp")
    public ResponseEntity<String> testOtpVerification(
            @RequestParam String email,
            @RequestParam String otp) {
        try {
            boolean isValid = otpService.verifyOtp(email, otp, "REGISTRATION");
            if (isValid) {
                return ResponseEntity.ok("OTP verified successfully!");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired OTP!");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to verify OTP: " + e.getMessage());
        }
    }

    // Test welcome email sending
    @PostMapping("/api/test/welcome-email")
    public ResponseEntity<String> testWelcomeEmail(
            @RequestParam String email,
            @RequestParam String username) {
        try {
            emailService.sendWelcomeEmail(email, username);
            return ResponseEntity.ok("Welcome email sent to " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send welcome email: " + e.getMessage());
        }
    }

    // Check OTP status for an email
    @GetMapping("/api/test/otp-status")
    public ResponseEntity<String> checkOtpStatus(@RequestParam String email) {
        try {
            // This endpoint can be used to check if there's a pending OTP for an email
            return ResponseEntity.ok("OTP status check for: " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to check OTP status: " + e.getMessage());
        }
    }
}
