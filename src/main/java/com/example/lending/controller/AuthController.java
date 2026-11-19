package com.example.lending.controller;

import com.example.lending.dto.GoogleAuthRequest;
import com.example.lending.dto.LoginRequest;
import com.example.lending.dto.LoginResponse;
import com.example.lending.dto.SignupRequest;
import com.example.lending.entity.User;
import com.example.lending.repository.UserRepository;
import com.example.lending.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id:}")
    private String configuredGoogleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("USER")
                .replace("ROLE_", "");

        String token = jwtTokenProvider.generateToken(username);

        return ResponseEntity.ok(new LoginResponse(token, username, role));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("message", "Username is already taken"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN") // Default to ADMIN for ledger management
                .build();

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap("message", "User registered successfully"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleAuthRequest request) {
        String email = null;

        try {
            // 1. Verify via ID Token if provided
            if (request.getIdToken() != null && !request.getIdToken().trim().isEmpty()) {
                String verifyUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + request.getIdToken().trim();
                Map<?, ?> response = restTemplate.getForObject(verifyUrl, Map.class);
                if (response != null) {
                    String aud = (String) response.get("aud");
                    if (configuredGoogleClientId != null && !configuredGoogleClientId.trim().isEmpty()) {
                        String[] allowedClients = configuredGoogleClientId.split(",");
                        boolean match = false;
                        for (String allowed : allowedClients) {
                            if (allowed.trim().equals(aud)) {
                                match = true;
                                break;
                            }
                        }
                        // Also allow Android/Web client IDs sharing the same Google Cloud Project ID prefix
                        if (!match && aud != null && configuredGoogleClientId.contains("-")) {
                            String projectPrefix = configuredGoogleClientId.substring(0, configuredGoogleClientId.indexOf('-')).trim();
                            if (!projectPrefix.isEmpty() && aud.startsWith(projectPrefix)) {
                                match = true;
                            }
                        }
                        if (!match) {
                            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                    .body(Collections.singletonMap("message", "Invalid Google Client ID audience"));
                        }
                    }
                    email = (String) response.get("email");
                }
            }
            // 2. Or verify via Access Token if provided
            else if (request.getAccessToken() != null && !request.getAccessToken().trim().isEmpty()) {
                String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.setBearerAuth(request.getAccessToken().trim());
                org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, org.springframework.http.HttpMethod.GET, entity, Map.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    email = (String) response.getBody().get("email");
                }
            }

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Could not verify Google authentication token"));
            }

            // Find or link user by email or username prefix (case-insensitive)
            final String userEmail = email.trim().toLowerCase();
            String emailPrefix = userEmail.contains("@") ? userEmail.substring(0, userEmail.indexOf('@')) : userEmail;

            User user = userRepository.findByUsernameIgnoreCase(userEmail)
                    .or(() -> userRepository.findByUsernameIgnoreCase(emailPrefix))
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .username(userEmail)
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .role("ADMIN") // Default to ADMIN for ledger management
                                .build();
                        return userRepository.save(newUser);
                    });

            String token = jwtTokenProvider.generateToken(user.getUsername());
            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), user.getRole()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "Google verification failed: " + e.getMessage()));
        }
    }
}
