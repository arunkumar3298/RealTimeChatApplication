package com.arun.chatapp.repository;

import com.arun.chatapp.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtpTypeAndIsVerifiedFalse(String email, String otpType);

    Optional<OtpVerification> findByEmailAndOtpAndOtpTypeAndIsVerifiedFalse(String email, String otp, String otpType);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.email = ?1 AND o.otpType = ?2")
    void deleteByEmailAndOtpType(String email, String otpType);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpVerification o WHERE o.expiresAt < ?1")
    void deleteExpiredOtps(LocalDateTime now);
}
