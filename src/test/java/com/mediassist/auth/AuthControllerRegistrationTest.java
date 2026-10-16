package com.mediassist.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediassist.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerRegistrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthControllerRegistrationTest(MockMvc mockMvc,
                                          ObjectMapper objectMapper,
                                          UserRepository userRepository,
                                          PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    @DisplayName("POST /api/auth/register should create patient and return HTTP 201 without password")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .name("Test Patient")
                .email("patient@example.com")
                .password("StrongPassword123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Patient"))
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        // Ensure password hash is not exposed anywhere in the response body
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).doesNotContain("password");

        // Verify database persistence and BCrypt hashing
        Optional<User> savedUser = userRepository.findByEmail("patient@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getPassword()).isNotEqualTo("StrongPassword123");
        assertThat(passwordEncoder.matches("StrongPassword123", savedUser.get().getPassword())).isTrue();
        assertThat(savedUser.get().getRole()).isEqualTo(Role.PATIENT);
        assertThat(savedUser.get().getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    @DisplayName("POST /api/auth/register should return HTTP 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        RegisterRequest initialRequest = RegisterRequest.builder()
                .name("First User")
                .email("duplicate@example.com")
                .password("StrongPassword123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialRequest)))
                .andExpect(status().isCreated());

        RegisterRequest duplicateRequest = RegisterRequest.builder()
                .name("Second User")
                .email("DUPLICATE@example.com")
                .password("DifferentPassword123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value(containsString("already registered")));
    }

    @Test
    @DisplayName("POST /api/auth/register should return HTTP 400 when validation fails")
    void shouldRejectInvalidRequestData() throws Exception {
        RegisterRequest invalidRequest = RegisterRequest.builder()
                .name("")
                .email("not-an-email")
                .password("short")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/auth/register should ignore client role and always assign PATIENT")
    void shouldEnforcePatientRoleRegardlessOfClientPayload() throws Exception {
        String payloadWithAdminRole = """
                {
                    "name": "Attacker",
                    "email": "attacker@example.com",
                    "password": "StrongPassword123",
                    "role": "ADMIN"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payloadWithAdminRole))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("PATIENT"));

        Optional<User> persisted = userRepository.findByEmail("attacker@example.com");
        assertThat(persisted).isPresent();
        assertThat(persisted.get().getRole()).isEqualTo(Role.PATIENT);
    }
}
