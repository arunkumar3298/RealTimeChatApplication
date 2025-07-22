package com.arun.chatapp.service;

import com.arun.chatapp.entity.OtpVerification;
import com.arun.chatapp.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max.attempts:3}")
    private int maxAttempts;

    public String generateAndSendOtp(String email, String otpType) {
        // Clean up any existing OTP for this email and type
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        // Generate new OTP
        String otp = generateOtp();

        // Save OTP to database
        OtpVerification otpEntity = new OtpVerification(
                email,
                otp,
                otpType,
                LocalDateTime.now().plusMinutes(otpExpiryMinutes)
        );
        otpRepository.save(otpEntity);

        // Send email
        emailService.sendOtpEmail(email, otp, otpType);

        return "OTP sent successfully to " + email;
    }

    public boolean verifyOtp(String email, String otp, String otpType) {
        Optional<OtpVerification> otpEntityOpt = otpRepository
                .findByEmailAndOtpTypeAndIsVerifiedFalse(email, otpType);

        if (otpEntityOpt.isEmpty()) {
            return false; // No OTP found
        }

        OtpVerification otpEntity = otpEntityOpt.get();

        // Check if OTP is expired
        if (otpEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false; // OTP expired
        }

        // Check if too many attempts
        if (otpEntity.getAttempts() >= maxAttempts) {
            return false; // Too many attempts
        }

        // Increment attempts
        otpEntity.setAttempts(otpEntity.getAttempts() + 1);
        otpRepository.save(otpEntity);

        // Check if OTP matches
        if (!otpEntity.getOtp().equals(otp)) {
            return false; // Invalid OTP
        }

        // Mark as verified
        otpEntity.setIsVerified(true);
        otpRepository.save(otpEntity);

        return true;
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}
