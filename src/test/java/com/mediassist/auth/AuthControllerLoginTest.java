package com.mediassist.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediassist.auth.dto.LoginRequest;
import com.mediassist.security.CustomUserDetailsService;
import com.mediassist.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerLoginTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public AuthControllerLoginTest(MockMvc mockMvc,
                                   ObjectMapper objectMapper,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   JwtService jwtService,
                                   CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @BeforeEach
    void setUp() {
        User activeUser = User.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password(passwordEncoder.encode("ValidPassword123"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(activeUser);

        User inactiveUser = User.builder()
                .name("Inactive User")
                .email("inactive@example.com")
                .password(passwordEncoder.encode("ValidPassword123"))
                .role(Role.PATIENT)
                .status(Status.INACTIVE)
                .build();
        userRepository.save(inactiveUser);
    }

    @Test
    @DisplayName("POST /api/auth/login should authenticate successfully and return signed JWT and safe user")
    void shouldLoginSuccessfullyAndReturnJwt() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("ValidPassword123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.name").value("Jane Doe"))
                .andExpect(jsonPath("$.user.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.user.role").value("PATIENT"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("password");

        // Verify the extracted JWT
        String token = objectMapper.readTree(responseBody).get("accessToken").asText();
        assertThat(token).isNotEmpty();
        assertThat(jwtService.extractUsername(token)).isEqualTo("jane.doe@example.com");

        UserDetails userDetails = userDetailsService.loadUserByUsername("jane.doe@example.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("POST /api/auth/login should return HTTP 401 when password is wrong")
    void shouldFailLoginWithIncorrectPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("WrongPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return HTTP 401 without revealing non-existent email")
    void shouldFailLoginWithNonExistentEmail() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("SomePassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("POST /api/auth/login should reject inactive user accounts with HTTP 401")
    void shouldFailLoginWithInactiveAccount() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("inactive@example.com")
                .password("ValidPassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("JWT validation should correctly identify and reject expired tokens")
    void shouldRejectExpiredJwtToken() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("jane.doe@example.com");
        String expiredToken = jwtService.generateTokenWithCustomExpiration(userDetails, 1L, "PATIENT", -5000);

        assertThat(jwtService.isTokenExpired(expiredToken)).isTrue();
        assertThat(jwtService.isTokenValid(expiredToken, userDetails)).isFalse();
    }

    @Test
    @DisplayName("JWT validation should reject tampered and malformed tokens")
    void shouldRejectTamperedToken() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("jane.doe@example.com");
        String validToken = jwtService.generateToken(userDetails, 1L, "PATIENT");
        String tamperedToken = validToken + "tampered";

        assertThat(jwtService.isTokenValid(tamperedToken, userDetails)).isFalse();
        assertThat(jwtService.isTokenValid("malformed.jwt.token", userDetails)).isFalse();
    }
}
