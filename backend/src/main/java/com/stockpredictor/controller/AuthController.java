package com.stockpredictor.controller;

import com.stockpredictor.model.User;
import com.stockpredictor.repository.UserRepository;
import com.stockpredictor.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @RestController = this class handles HTTP requests and returns JSON
// @RequestMapping = all routes here start with /api/auth
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    // POST /api/auth/register
    // Frontend sends: { username, email, password, fullName }
    // We return: { message: "User registered successfully" }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String fullName = request.get("fullName");

        // Validation checks
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // Create new user - note we HASH the password before saving
        // BCrypt turns "mypassword123" into something like "$2a$10$xK8..."
        // Even if someone steals the database, they can't recover the original password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // POST /api/auth/login
    // Frontend sends: { username, password }
    // We return: { token: "eyJhbGci...", username: "john" }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        try {
            // Spring Security checks username + password for us
            // It calls UserDetailsServiceImpl.loadUserByUsername() then compares hashed passwords
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // If we get here, credentials are correct - generate a JWT token
            String token = jwtUtils.generateToken(username);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }
}
