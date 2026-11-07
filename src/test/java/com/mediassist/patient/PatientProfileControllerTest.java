package com.mediassist.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.patient.dto.PatientProfileCreateRequest;
import com.mediassist.patient.dto.PatientProfileUpdateRequest;
import com.mediassist.security.CustomUserDetailsService;
import com.mediassist.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PatientProfileControllerTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private User patientUser1;
    private String patient1Token;

    private User patientUser2;
    private String patient2Token;

    private String doctorToken;
    private String adminToken;

    @Autowired
    public PatientProfileControllerTest(MockMvc mockMvc,
                                        UserRepository userRepository,
                                        PatientProfileRepository patientProfileRepository,
                                        PasswordEncoder passwordEncoder,
                                        JwtService jwtService,
                                        CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        patientUser1 = userRepository.save(User.builder()
                .name("John Patient")
                .email("john.patient@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        UserDetails patient1Details = userDetailsService.loadUserByUsername("john.patient@example.com");
        patient1Token = jwtService.generateToken(patient1Details, patientUser1.getId(), "PATIENT");

        patientUser2 = userRepository.save(User.builder()
                .name("Jane Patient")
                .email("jane.patient@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        UserDetails patient2Details = userDetailsService.loadUserByUsername("jane.patient@example.com");
        patient2Token = jwtService.generateToken(patient2Details, patientUser2.getId(), "PATIENT");

        User doctorUser = userRepository.save(User.builder()
                .name("Dr. Marcus")
                .email("dr.marcus@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        UserDetails doctorDetails = userDetailsService.loadUserByUsername("dr.marcus@example.com");
        doctorToken = jwtService.generateToken(doctorDetails, doctorUser.getId(), "DOCTOR");

        User adminUser = userRepository.save(User.builder()
                .name("Admin Arthur")
                .email("admin.arthur@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin.arthur@example.com");
        adminToken = jwtService.generateToken(adminDetails, adminUser.getId(), "ADMIN");
    }

    @Nested
    @DisplayName("Successful Profile Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("POST /api/patient/profile creates a profile for authenticated patient (201 Created)")
        void shouldCreateProfileSuccessfully() throws Exception {
            PatientProfileCreateRequest request = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .phoneNumber("+1-555-123-4567")
                    .address("123 Maple Street, Springfield, IL")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.userId").value(patientUser1.getId()))
                    .andExpect(jsonPath("$.name").value("John Patient"))
                    .andExpect(jsonPath("$.email").value("john.patient@example.com"))
                    .andExpect(jsonPath("$.dateOfBirth").value("1990-05-15"))
                    .andExpect(jsonPath("$.gender").value("MALE"))
                    .andExpect(jsonPath("$.phoneNumber").value("+1-555-123-4567"))
                    .andExpect(jsonPath("$.address").value("123 Maple Street, Springfield, IL"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());

            assertThat(patientProfileRepository.existsByUserId(patientUser1.getId())).isTrue();
        }

        @Test
        @DisplayName("GET /api/patient/profile retrieves authenticated patient's profile (200 OK)")
        void shouldGetProfileSuccessfully() throws Exception {
            patientProfileRepository.save(PatientProfile.builder()
                    .user(patientUser1)
                    .dateOfBirth(LocalDate.of(1985, 3, 20))
                    .gender(Gender.MALE)
                    .phoneNumber("555-888-9999")
                    .address("456 Oak Avenue, Metropolis")
                    .build());

            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(patientUser1.getId()))
                    .andExpect(jsonPath("$.name").value("John Patient"))
                    .andExpect(jsonPath("$.email").value("john.patient@example.com"))
                    .andExpect(jsonPath("$.dateOfBirth").value("1985-03-20"))
                    .andExpect(jsonPath("$.gender").value("MALE"))
                    .andExpect(jsonPath("$.phoneNumber").value("555-888-9999"))
                    .andExpect(jsonPath("$.address").value("456 Oak Avenue, Metropolis"));
        }

        @Test
        @DisplayName("PUT /api/patient/profile updates authenticated patient's profile (200 OK)")
        void shouldUpdateProfileSuccessfully() throws Exception {
            patientProfileRepository.save(PatientProfile.builder()
                    .user(patientUser1)
                    .dateOfBirth(LocalDate.of(1992, 1, 1))
                    .gender(Gender.PREFER_NOT_TO_SAY)
                    .phoneNumber("555-000-1111")
                    .address("Original Address 100")
                    .build());

            PatientProfileUpdateRequest updateRequest = PatientProfileUpdateRequest.builder()
                    .dateOfBirth(LocalDate.of(1992, 1, 1))
                    .gender(Gender.OTHER)
                    .phoneNumber("+1-555-999-0000")
                    .address("Updated Suite 200, Innovation Blvd")
                    .build();

            mockMvc.perform(put("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(patientUser1.getId()))
                    .andExpect(jsonPath("$.gender").value("OTHER"))
                    .andExpect(jsonPath("$.phoneNumber").value("+1-555-999-0000"))
                    .andExpect(jsonPath("$.address").value("Updated Suite 200, Innovation Blvd"));

            PatientProfile updated = patientProfileRepository.findByUserId(patientUser1.getId()).orElseThrow();
            assertThat(updated.getAddress()).isEqualTo("Updated Suite 200, Innovation Blvd");
            assertThat(updated.getPhoneNumber()).isEqualTo("+1-555-999-0000");
            assertThat(updated.getGender()).isEqualTo(Gender.OTHER);
        }
    }

    @Nested
    @DisplayName("Security, RBAC and Ownership Enforcement")
    class SecurityAndOwnershipOperations {

        @Test
        @DisplayName("Unauthenticated request to POST/GET/PUT profile should return 401 Unauthorized")
        void shouldRejectUnauthenticatedRequests() throws Exception {
            PatientProfileCreateRequest request = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.FEMALE)
                    .phoneNumber("555-123-4567")
                    .address("123 Main Street")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            mockMvc.perform(get("/api/patient/profile"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            mockMvc.perform(put("/api/patient/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("DOCTOR role should be rejected with 403 Forbidden on patient profile endpoints")
        void shouldRejectDoctorRole() throws Exception {
            PatientProfileCreateRequest request = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1980, 1, 1))
                    .gender(Gender.MALE)
                    .phoneNumber("555-222-3333")
                    .address("789 Hospital Way")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + doctorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + doctorToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(put("/api/patient/profile")
                            .header("Authorization", "Bearer " + doctorToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("ADMIN role should be rejected with 403 Forbidden on patient profile endpoints")
        void shouldRejectAdminRole() throws Exception {
            PatientProfileCreateRequest request = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1980, 1, 1))
                    .gender(Gender.MALE)
                    .phoneNumber("555-222-3333")
                    .address("789 Admin Street")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("One patient cannot view or access another patient's profile")
        void shouldIsolateProfilesBetweenPatients() throws Exception {
            patientProfileRepository.save(PatientProfile.builder()
                    .user(patientUser1)
                    .dateOfBirth(LocalDate.of(1990, 5, 10))
                    .gender(Gender.MALE)
                    .phoneNumber("555-111-2222")
                    .address("Patient 1 Private Address")
                    .build());

            patientProfileRepository.save(PatientProfile.builder()
                    .user(patientUser2)
                    .dateOfBirth(LocalDate.of(1995, 8, 25))
                    .gender(Gender.FEMALE)
                    .phoneNumber("555-333-4444")
                    .address("Patient 2 Private Address")
                    .build());

            // Patient 1 gets profile -> only sees Patient 1
            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(patientUser1.getId()))
                    .andExpect(jsonPath("$.name").value("John Patient"))
                    .andExpect(jsonPath("$.address").value("Patient 1 Private Address"));

            // Patient 2 gets profile -> only sees Patient 2
            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient2Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(patientUser2.getId()))
                    .andExpect(jsonPath("$.name").value("Jane Patient"))
                    .andExpect(jsonPath("$.address").value("Patient 2 Private Address"));
        }

        @Test
        @DisplayName("Request body with spoofed user ID cannot reassign profile ownership")
        void shouldIgnoreSpoofedUserIdInRequestBody() throws Exception {
            String maliciousPayload = """
                    {
                        "userId": 999999,
                        "dateOfBirth": "1991-07-20",
                        "gender": "MALE",
                        "phoneNumber": "555-987-6543",
                        "address": "Valid Address Road"
                    }
                    """;

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(maliciousPayload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(patientUser1.getId()))
                    .andExpect(jsonPath("$.email").value("john.patient@example.com"));

            PatientProfile saved = patientProfileRepository.findByUserId(patientUser1.getId()).orElseThrow();
            assertThat(saved.getUser().getId()).isEqualTo(patientUser1.getId());
        }
    }

    @Nested
    @DisplayName("Validation and Conflict Error Handling")
    class ValidationAndErrorHandlingOperations {

        @Test
        @DisplayName("Duplicate profile creation should return 409 Conflict")
        void shouldRejectDuplicateProfileCreation() throws Exception {
            patientProfileRepository.save(PatientProfile.builder()
                    .user(patientUser1)
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .phoneNumber("+1-555-123-4567")
                    .address("123 Maple Street")
                    .build());

            PatientProfileCreateRequest duplicateRequest = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .gender(Gender.MALE)
                    .phoneNumber("+1-555-999-9999")
                    .address("Another Address")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        @DisplayName("GET on non-existent profile should return 404 Not Found")
        void shouldReturn404WhenProfileNotFoundOnGet() throws Exception {
            mockMvc.perform(get("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message", containsString("not found")));
        }

        @Test
        @DisplayName("PUT on non-existent profile should return 404 Not Found")
        void shouldReturn404WhenProfileNotFoundOnPut() throws Exception {
            PatientProfileUpdateRequest updateRequest = PatientProfileUpdateRequest.builder()
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.FEMALE)
                    .phoneNumber("555-111-2222")
                    .address("Nonexistent Profile Street")
                    .build();

            mockMvc.perform(put("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Validation fails when required fields are missing or invalid (400 Bad Request)")
        void shouldRejectInvalidFields() throws Exception {
            // Missing dateOfBirth, gender, blank phone, short address
            PatientProfileCreateRequest invalidRequest = PatientProfileCreateRequest.builder()
                    .dateOfBirth(null)
                    .gender(null)
                    .phoneNumber("")
                    .address("abc")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.dateOfBirth").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.gender").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.phoneNumber").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.address").isNotEmpty());
        }

        @Test
        @DisplayName("Future date of birth should be rejected (400 Bad Request)")
        void shouldRejectFutureDateOfBirth() throws Exception {
            PatientProfileCreateRequest futureDobRequest = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.now().plusDays(1))
                    .gender(Gender.MALE)
                    .phoneNumber("555-123-4567")
                    .address("Valid Address Street 123")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(futureDobRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.dateOfBirth").value("Date of birth must be in the past"));
        }

        @Test
        @DisplayName("Invalid phone number format should be rejected (400 Bad Request)")
        void shouldRejectInvalidPhoneNumberFormat() throws Exception {
            PatientProfileCreateRequest badPhoneRequest = PatientProfileCreateRequest.builder()
                    .dateOfBirth(LocalDate.of(1990, 1, 1))
                    .gender(Gender.FEMALE)
                    .phoneNumber("bad-phone-number!!")
                    .address("Valid Address Street 123")
                    .build();

            mockMvc.perform(post("/api/patient/profile")
                            .header("Authorization", "Bearer " + patient1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(badPhoneRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.phoneNumber").isNotEmpty());
        }
    }
}
