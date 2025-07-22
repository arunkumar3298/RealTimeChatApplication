package com.arun.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp, String otpType) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(getEmailSubject(otpType));
        message.setText(getEmailBody(otp, otpType));
        message.setFrom("noreply@chatapp.com");

        mailSender.send(message);
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Chat Application!");
        message.setText("Hi " + username + ",\n\nWelcome to Arun's Chat Application! Your account has been successfully verified.\n\nStart chatting and connect with others!\n\nBest regards,\nArun's Chat Application Team");
        message.setFrom("noreply@chatapp.com");

        mailSender.send(message);
    }

    // NEW - Password Reset Success Email
    public void sendPasswordResetSuccessEmail(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset Successful - Arun's Chat Application");
        message.setText(getPasswordResetSuccessEmailBody(username));
        message.setFrom("noreply@chatapp.com");

        mailSender.send(message);
    }

    // NEW - Password Reset OTP Email
    public void sendPasswordResetOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP - Arun's Chat Application");
        message.setText(getPasswordResetOtpEmailBody(otp));
        message.setFrom("noreply@chatapp.com");

        mailSender.send(message);
    }

    private String getEmailSubject(String otpType) {
        switch (otpType.toUpperCase()) {
            case "REGISTRATION":
                return "Verify Your Email - Arun's Chat Application";
            case "PASSWORD_RESET":
                return "Password Reset OTP - Arun's Chat Application";
            default:
                return "Email Verification - Arun's Chat Application";
        }
    }

    private String getEmailBody(String otp, String otpType) {
        StringBuilder body = new StringBuilder();

        if ("REGISTRATION".equals(otpType.toUpperCase())) {
            body.append("Welcome to Arun's Chat Application!\n\n");
            body.append("Thank you for registering. Please verify your email address using the OTP below:\n\n");
        } else if ("PASSWORD_RESET".equals(otpType.toUpperCase())) {
            body.append("Password Reset Request\n\n");
            body.append("You requested a password reset for your Arun's Chat Application account.\n\n");
            body.append("Use the following OTP to reset your password:\n\n");
        }

        body.append("Your OTP: ").append(otp).append("\n\n");
        body.append("This OTP will expire in 5 minutes.\n");

        if ("PASSWORD_RESET".equals(otpType.toUpperCase())) {
            body.append("If you didn't request this password reset, please ignore this email ");
            body.append("and your password will remain unchanged.\n");
            body.append("For security reasons, please do not share this OTP with anyone.\n\n");
        } else {
            body.append("If you didn't request this, please ignore this email.\n\n");
        }

        body.append("Best regards,\n");
        body.append("Arun's Chat Application Team");

        return body.toString();
    }

    // NEW - Password Reset Success Email Body
    private String getPasswordResetSuccessEmailBody(String username) {
        StringBuilder body = new StringBuilder();

        body.append("Password Reset Successful\n\n");
        body.append("Hi ").append(username).append(",\n\n");
        body.append("Your password has been successfully reset for your Arun's Chat Application account.\n\n");
        body.append("You can now login with your new password.\n\n");
        body.append("If you didn't make this change, please contact our support team immediately.\n\n");
        body.append("For your security:\n");
        body.append("- Keep your new password secure\n");
        body.append("- Don't share it with anyone\n");
        body.append("- Consider using a strong, unique password\n\n");
        body.append("Best regards,\n");
        body.append("Arun's Chat Application Team");

        return body.toString();
    }

    // NEW - Password Reset OTP Email Body
    private String getPasswordResetOtpEmailBody(String otp) {
        StringBuilder body = new StringBuilder();

        body.append("Password Reset Request\n\n");
        body.append("You requested a password reset for your Arun's Chat Application account.\n\n");
        body.append("Use the following OTP to reset your password:\n\n");
        body.append("Your OTP: ").append(otp).append("\n\n");
        body.append("This OTP will expire in 5 minutes.\n\n");
        body.append("If you didn't request this password reset, please ignore this email ");
        body.append("and your password will remain unchanged.\n\n");
        body.append("For security reasons:\n");
        body.append("- Do not share this OTP with anyone\n");
        body.append("- Use it only on the official password reset page\n");
        body.append("- If you suspect suspicious activity, contact support\n\n");
        body.append("Best regards,\n");
        body.append("Arun's Chat Application Team");

        return body.toString();
    }
}
