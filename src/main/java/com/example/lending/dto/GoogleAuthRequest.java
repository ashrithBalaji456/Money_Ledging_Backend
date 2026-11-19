package com.example.lending.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleAuthRequest {
    private Object idToken;
    private Object accessToken;
    private String email;
    private String name;

    public String resolveIdToken() {
        if (idToken == null) return null;
        if (idToken instanceof String s) return s.trim();
        if (idToken instanceof Map<?, ?> m) {
            Object token = m.get("token") != null ? m.get("token") : (m.get("jwt") != null ? m.get("jwt") : m.get("idToken"));
            return token != null ? token.toString().trim() : null;
        }
        return idToken.toString().trim();
    }

    public String resolveAccessToken() {
        if (accessToken == null) return null;
        if (accessToken instanceof String s) return s.trim();
        if (accessToken instanceof Map<?, ?> m) {
            Object token = m.get("token") != null ? m.get("token") : (m.get("accessToken") != null ? m.get("accessToken") : m.get("value"));
            return token != null ? token.toString().trim() : null;
        }
        return accessToken.toString().trim();
    }
}
