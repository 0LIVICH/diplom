package ru.netology.cloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    @JsonProperty("auth-token")
    private String authToken;

    public LoginResponse(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
}


