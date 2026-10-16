package com.mediassist.auth;

import com.mediassist.auth.dto.AuthResponse;
import com.mediassist.auth.dto.LoginRequest;
import com.mediassist.auth.dto.RegisterRequest;
import com.mediassist.auth.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

/**
 * Controller handling user authentication, profile, and authorization demonstration requests.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user with the default PATIENT role.
     *
     * @param request user registration details
     * @return HTTP 201 with the created user's safe profile
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a signed JWT with profile details.
     *
     * @param request user login credentials
     * @return HTTP 200 with JWT and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     * Identity is resolved strictly from the authenticated security context.
     *
     * @param userDetails the authenticated principal
     * @return HTTP 200 with safe user details
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint accessible only to users with the PATIENT role.
     */
    @GetMapping("/patient-only")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Map<String, String>> patientOnly() {
        return ResponseEntity.ok(Collections.singletonMap("message", "Access granted to PATIENT"));
    }

    /**
     * Protected endpoint accessible only to users with the DOCTOR role.
     */
    @GetMapping("/doctor-only")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Map<String, String>> doctorOnly() {
        return ResponseEntity.ok(Collections.singletonMap("message", "Access granted to DOCTOR"));
    }

    /**
     * Protected endpoint accessible only to users with the ADMIN role.
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Collections.singletonMap("message", "Access granted to ADMIN"));
    }
}
