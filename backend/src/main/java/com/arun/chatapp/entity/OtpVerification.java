package com.arun.chatapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verification")
public class OtpVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(name = "otp_type")
    private String otpType; // REGISTRATION, PASSWORD_RESET

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "attempts")
    private Integer attempts = 0;

    // Constructors
    public OtpVerification() {}

    public OtpVerification(String email, String otp, String otpType, LocalDateTime expiresAt) {
        this.email = email;
        this.otp = otp;
        this.otpType = otpType;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public String getOtpType() { return otpType; }
    public void setOtpType(String otpType) { this.otpType = otpType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }
}
