package com.tl.reap_admin_api.dto;

public class JwtAuthenticationResponse {
    private String accessToken;
    private int role;
    private String tokenType = "Bearer";

    public JwtAuthenticationResponse(String accessToken, int role) {
        this.accessToken = accessToken;
        this.role = role;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}