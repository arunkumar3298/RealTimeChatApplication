package com.arun.chatapp.dto;

public class RegistrationRequest {
    private String username;
    private String email;
    private String password;

    // Default constructor (REQUIRED for JSON deserialization)
    public RegistrationRequest() {}

    public RegistrationRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters (REQUIRED)
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // toString method for debugging
    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
