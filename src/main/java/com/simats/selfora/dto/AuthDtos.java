package com.simats.selfora.dto;

import java.util.List;

public class AuthDtos {

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class AuthResponse {
        private String token;
        private String tokenType;
        private Long userId;
        private String username;
        private String fullName;
        private List<String> roles;

        public AuthResponse() {}

        public AuthResponse(String token, String tokenType, Long userId, String username, String fullName, List<String> roles) {
            this.token = token;
            this.tokenType = tokenType;
            this.userId = userId;
            this.username = username;
            this.fullName = fullName;
            this.roles = roles;
        }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String token;
            private String tokenType;
            private Long userId;
            private String username;
            private String fullName;
            private List<String> roles;

            public Builder token(String token) { this.token = token; return this; }
            public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
            public Builder userId(Long userId) { this.userId = userId; return this; }
            public Builder username(String username) { this.username = username; return this; }
            public Builder fullName(String fullName) { this.fullName = fullName; return this; }
            public Builder roles(List<String> roles) { this.roles = roles; return this; }
            public AuthResponse build() { return new AuthResponse(token, tokenType, userId, username, fullName, roles); }
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }
}
