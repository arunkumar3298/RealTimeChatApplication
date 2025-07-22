package com.arun.chatapp.controller;

import com.arun.chatapp.dto.EmailVerificationRequest;
import com.arun.chatapp.dto.LoginRequest;
import com.arun.chatapp.dto.PasswordResetRequest;
import com.arun.chatapp.dto.PasswordResetVerification;
import com.arun.chatapp.dto.RegistrationRequest;
import com.arun.chatapp.entity.User;
import com.arun.chatapp.service.AuthService;
import com.arun.chatapp.service.EmailService;
import com.arun.chatapp.service.OtpService;
import com.arun.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    // Your existing methods stay exactly the same
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            // DEBUG LOGGING - Add these lines to see what's happening
            System.out.println("=== REGISTRATION REQUEST DEBUG ===");
            System.out.println("Received request: " + request);
            System.out.println("Username: '" + request.getUsername() + "'");
            System.out.println("Email: '" + request.getEmail() + "'");
            System.out.println("Password length: " + (request.getPassword() != null ? request.getPassword().length() : "null"));

            // Validation
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                System.out.println("ERROR: Username is null or empty");
                return ResponseEntity.badRequest().body("Username is required");
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                System.out.println("ERROR: Email is null or empty");
                return ResponseEntity.badRequest().body("Email is required");
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                System.out.println("ERROR: Password is null or empty");
                return ResponseEntity.badRequest().body("Password is required");
            }

            System.out.println("Basic validation passed, checking duplicates...");

            // Check duplicates
            if (userService.existsByUsername(request.getUsername())) {
                System.out.println("ERROR: Username already exists - " + request.getUsername());
                return ResponseEntity.badRequest()
                        .body("Username is already taken!");
            }

            if (userService.existsByEmail(request.getEmail())) {
                System.out.println("ERROR: Email already exists - " + request.getEmail());
                return ResponseEntity.badRequest()
                        .body("Email is already registered!");
            }

            System.out.println("Duplicate check passed, creating user...");

            // Create unverified user
            User user = new User(request.getUsername(), request.getEmail(), request.getPassword());
            user.setIsEmailVerified(false);

            System.out.println("User object created, saving to database...");
            User savedUser = userService.createUser(user);
            System.out.println("User saved with ID: " + savedUser.getId());

            System.out.println("Generating and sending OTP...");
            // Send verification email
            otpService.generateAndSendOtp(request.getEmail(), "REGISTRATION");
            System.out.println("OTP sent successfully");

            System.out.println("Registration completed successfully");
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful! Please check your email for verification code.",
                    "email", request.getEmail(),
                    "nextStep", "verify-email"
            ));

        } catch (Exception e) {
            System.err.println("REGISTRATION EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerificationRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP is required");
            }

            // Verify OTP
            boolean isValidOtp = otpService.verifyOtp(request.getEmail(), request.getOtp(), "REGISTRATION");

            if (!isValidOtp) {
                return ResponseEntity.badRequest()
                        .body("Invalid or expired OTP. Please try again.");
            }

            // Update user verification status
            var userOpt = userService.getUserByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();
            user.setIsEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userService.updateUser(user);

            // Send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "message", "Email verified successfully! Your account is now active.",
                    "user", Map.of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail(),
                            "emailVerified", user.getIsEmailVerified()
                    ),
                    "nextStep", "login"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Email verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            // Validation
            if (loginRequest.getUsername() == null || loginRequest.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }

            if (loginRequest.getPassword() == null || loginRequest.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            // Check if user exists and is verified
            var userOpt = userService.getUserByUsername(loginRequest.getUsername());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid username or password");
            }

            User user = userOpt.get();
            if (!user.getIsEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Please verify your email before logging in",
                        "email", user.getEmail(),
                        "nextStep", "verify-email"
                ));
            }

            // Authenticate user
            Map<String, Object> authResult = authService.authenticateUser(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            boolean success = (boolean) authResult.get("success");

            if (success) {
                return ResponseEntity.ok(authResult);
            } else {
                return ResponseEntity.badRequest().body(authResult.get("message"));
            }

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerificationOtp(@RequestParam String email) {
        try {
            // Check if user exists
            var userOpt = userService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();
            if (user.getIsEmailVerified()) {
                return ResponseEntity.badRequest().body("Email is already verified");
            }

            // Send new OTP
            otpService.generateAndSendOtp(email, "REGISTRATION");

            return ResponseEntity.ok("Verification code resent to " + email);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to resend verification code");
        }
    }

    // NEW - PASSWORD RESET ENDPOINTS

    // Step 1: Request Password Reset (Send OTP to email)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            // Check if user exists
            var userOpt = userService.getUserByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                // For security, don't reveal if email exists or not
                return ResponseEntity.ok(Map.of(
                        "message", "If an account with this email exists, you will receive a password reset code shortly.",
                        "email", request.getEmail(),
                        "nextStep", "verify-reset-otp"
                ));
            }

            User user = userOpt.get();

            // Check if user account is verified
            if (!user.getIsEmailVerified()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Please verify your email address first before resetting password.",
                        "email", user.getEmail(),
                        "nextStep", "verify-email"
                ));
            }

            // Generate and send OTP for password reset
            otpService.generateAndSendOtp(request.getEmail(), "PASSWORD_RESET");

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset code sent to your email address.",
                    "email", request.getEmail(),
                    "nextStep", "verify-reset-otp"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to process password reset request: " + e.getMessage());
        }
    }

    // Step 2: Verify OTP and Reset Password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetVerification request) {
        try {
            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }

            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("OTP is required");
            }

            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("New password is required");
            }

            // Validate password strength
            if (request.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body("Password must be at least 6 characters long");
            }

            // Verify OTP for password reset
            boolean isValidOtp = otpService.verifyOtp(request.getEmail(), request.getOtp(), "PASSWORD_RESET");

            if (!isValidOtp) {
                return ResponseEntity.badRequest()
                        .body("Invalid or expired OTP. Please request a new password reset.");
            }

            // Check if user exists (double-check)
            var userOpt = userService.getUserByEmail(request.getEmail());
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOpt.get();

            // Update user password
            userService.updateUserPassword(request.getEmail(), request.getNewPassword());

            // Send password reset success email
            emailService.sendPasswordResetSuccessEmail(user.getEmail(), user.getUsername());

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successful! You can now login with your new password.",
                    "nextStep", "login"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to reset password: " + e.getMessage());
        }
    }

    // Step 3: Resend Password Reset OTP
    @PostMapping("/resend-password-reset")
    public ResponseEntity<?> resendPasswordResetOtp(@RequestParam String email) {
        try {
            // Check if user exists and is verified
            var userOpt = userService.getUserByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok("If an account with this email exists, a new reset code will be sent.");
            }

            User user = userOpt.get();
            if (!user.getIsEmailVerified()) {
                return ResponseEntity.badRequest()
                        .body("Please verify your email address first.");
            }

            // Send new OTP
            otpService.generateAndSendOtp(email, "PASSWORD_RESET");

            return ResponseEntity.ok("Password reset code resent to " + email);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to resend password reset code");
        }
    }
}
