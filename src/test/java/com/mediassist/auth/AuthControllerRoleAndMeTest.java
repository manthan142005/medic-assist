package com.mediassist.auth;

import com.mediassist.security.CustomUserDetailsService;
import com.mediassist.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerRoleAndMeTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    private String patientToken;
    private String doctorToken;
    private String adminToken;

    @Autowired
    public AuthControllerRoleAndMeTest(MockMvc mockMvc,
                                       UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       JwtService jwtService,
                                       CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @BeforeEach
    void setUp() {
        User patient = User.builder()
                .name("Alice Patient")
                .email("alice.patient@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build();
        User savedPatient = userRepository.save(patient);
        UserDetails patientDetails = userDetailsService.loadUserByUsername("alice.patient@example.com");
        patientToken = jwtService.generateToken(patientDetails, savedPatient.getId(), "PATIENT");

        User doctor = User.builder()
                .name("Dr. Bob")
                .email("dr.bob@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build();
        User savedDoctor = userRepository.save(doctor);
        UserDetails doctorDetails = userDetailsService.loadUserByUsername("dr.bob@example.com");
        doctorToken = jwtService.generateToken(doctorDetails, savedDoctor.getId(), "DOCTOR");

        User admin = User.builder()
                .name("Carol Admin")
                .email("carol.admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();
        User savedAdmin = userRepository.save(admin);
        UserDetails adminDetails = userDetailsService.loadUserByUsername("carol.admin@example.com");
        adminToken = jwtService.generateToken(adminDetails, savedAdmin.getId(), "ADMIN");
    }

    @Test
    @DisplayName("GET /api/auth/me should reject unauthenticated request with HTTP 401")
    void shouldRejectMeWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/auth/me should reject invalid or expired token with HTTP 401")
    void shouldRejectMeWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        UserDetails patientDetails = userDetailsService.loadUserByUsername("alice.patient@example.com");
        String expiredToken = jwtService.generateTokenWithCustomExpiration(patientDetails, 1L, "PATIENT", -5000);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("GET /api/auth/me should return current user profile with valid JWT without password")
    void shouldReturnProfileWithValidJwt() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Patient"))
                .andExpect(jsonPath("$.email").value("alice.patient@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).doesNotContain("password");
    }

    @Test
    @DisplayName("Role Authorization: Unauthenticated requests to role endpoints return HTTP 401")
    void shouldRejectUnauthenticatedRoleEndpoints() throws Exception {
        mockMvc.perform(get("/api/auth/patient-only"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/doctor-only"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/auth/admin-only"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Role Authorization: PATIENT can access patient-only, but is forbidden from doctor and admin endpoints")
    void shouldEnforcePatientPermissions() throws Exception {
        // Patient accessing patient endpoint -> 200
        mockMvc.perform(get("/api/auth/patient-only")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to PATIENT"));

        // Patient accessing doctor endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/auth/doctor-only")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));

        // Patient accessing admin endpoint -> 403 Forbidden
        mockMvc.perform(get("/api/auth/admin-only")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    @DisplayName("Role Authorization: DOCTOR can access doctor-only, but is forbidden from admin endpoint")
    void shouldEnforceDoctorPermissions() throws Exception {
        mockMvc.perform(get("/api/auth/doctor-only")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to DOCTOR"));

        mockMvc.perform(get("/api/auth/admin-only")
                        .header("Authorization", "Bearer " + doctorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("Role Authorization: ADMIN can access admin-only endpoint")
    void shouldEnforceAdminPermissions() throws Exception {
        mockMvc.perform(get("/api/auth/admin-only")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Access granted to ADMIN"));
    }
}
