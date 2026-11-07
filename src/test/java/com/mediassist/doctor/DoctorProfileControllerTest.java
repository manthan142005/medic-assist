package com.mediassist.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.dto.DoctorProfileCreateRequest;
import com.mediassist.doctor.dto.DoctorProfileUpdateRequest;
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

import java.math.BigDecimal;

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
class DoctorProfileControllerTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private User doctorUser1;
    private String doctor1Token;

    private User doctorUser2;
    private String doctor2Token;

    private String patientToken;
    private String adminToken;
    private User adminUser;
    private DoctorProfile savedDoctor1Profile;

    @Autowired
    public DoctorProfileControllerTest(MockMvc mockMvc,
                                       UserRepository userRepository,
                                       DoctorProfileRepository doctorProfileRepository,
                                       PasswordEncoder passwordEncoder,
                                       JwtService jwtService,
                                       CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        doctorUser1 = userRepository.save(User.builder()
                .name("Dr. Sarah Collins")
                .email("dr.sarah@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        UserDetails doctor1Details = userDetailsService.loadUserByUsername("dr.sarah@example.com");
        doctor1Token = jwtService.generateToken(doctor1Details, doctorUser1.getId(), "DOCTOR");

        doctorUser2 = userRepository.save(User.builder()
                .name("Dr. James Reed")
                .email("dr.james@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        UserDetails doctor2Details = userDetailsService.loadUserByUsername("dr.james@example.com");
        doctor2Token = jwtService.generateToken(doctor2Details, doctorUser2.getId(), "DOCTOR");

        User patientUser = userRepository.save(User.builder()
                .name("Alice Patient")
                .email("alice.patient.doc@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        UserDetails patientDetails = userDetailsService.loadUserByUsername("alice.patient.doc@example.com");
        patientToken = jwtService.generateToken(patientDetails, patientUser.getId(), "PATIENT");

        adminUser = userRepository.save(User.builder()
                .name("Admin Grace")
                .email("admin.grace@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin.grace@example.com");
        adminToken = jwtService.generateToken(adminDetails, adminUser.getId(), "ADMIN");
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private DoctorProfileCreateRequest validCreateRequest() {
        return DoctorProfileCreateRequest.builder()
                .displayName("Dr. Sarah Collins")
                .specialization("Cardiology")
                .qualifications("MBBS, MD (Cardiology), FACC")
                .yearsOfExperience(12)
                .clinicName("HeartCare Clinic")
                .clinicAddress("45 Medical Park, New York, NY 10001")
                .consultationFee(new BigDecimal("150.00"))
                .bio("Specialist in interventional cardiology with 12 years of experience.")
                .phoneNumber("+1-555-200-3000")
                .build();
    }

    private DoctorProfile persistDoctor1Profile() {
        return doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser1)
                .displayName("Dr. Sarah Collins")
                .specialization("Cardiology")
                .qualifications("MBBS, MD (Cardiology)")
                .yearsOfExperience(12)
                .clinicName("HeartCare Clinic")
                .clinicAddress("45 Medical Park, New York, NY")
                .consultationFee(new BigDecimal("150.00"))
                .bio("Experienced cardiologist.")
                .phoneNumber("+1-555-200-3000")
                .build());
    }

    // ─── Successful Profile Operations ────────────────────────────────────────

    @Nested
    @DisplayName("Successful Profile Operations")
    class SuccessfulOperations {

        @Test
        @DisplayName("POST /api/doctor/profile creates a profile for authenticated doctor (201 Created)")
        void shouldCreateProfileSuccessfully() throws Exception {
            DoctorProfileCreateRequest request = validCreateRequest();

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.name").value("Dr. Sarah Collins"))
                    .andExpect(jsonPath("$.email").value("dr.sarah@example.com"))
                    .andExpect(jsonPath("$.displayName").value("Dr. Sarah Collins"))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"))
                    .andExpect(jsonPath("$.qualifications").value("MBBS, MD (Cardiology), FACC"))
                    .andExpect(jsonPath("$.yearsOfExperience").value(12))
                    .andExpect(jsonPath("$.clinicName").value("HeartCare Clinic"))
                    .andExpect(jsonPath("$.clinicAddress").value("45 Medical Park, New York, NY 10001"))
                    .andExpect(jsonPath("$.consultationFee").value(150.00))
                    .andExpect(jsonPath("$.bio").value("Specialist in interventional cardiology with 12 years of experience."))
                    .andExpect(jsonPath("$.phoneNumber").value("+1-555-200-3000"))
                    .andExpect(jsonPath("$.createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.updatedAt").isNotEmpty());

            assertThat(doctorProfileRepository.existsByUserId(doctorUser1.getId())).isTrue();
        }

        @Test
        @DisplayName("POST /api/doctor/profile creates profile with optional fields omitted (201 Created)")
        void shouldCreateProfileWithOptionalFieldsOmitted() throws Exception {
            DoctorProfileCreateRequest request = DoctorProfileCreateRequest.builder()
                    .displayName("Dr. Sarah Collins")
                    .specialization("Cardiology")
                    .qualifications("MBBS, MD (Cardiology)")
                    .yearsOfExperience(10)
                    .clinicName("City Hospital")
                    .clinicAddress("100 Main Street, Chicago, IL 60601")
                    .build(); // no consultationFee, bio, or phoneNumber

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"));

            assertThat(doctorProfileRepository.existsByUserId(doctorUser1.getId())).isTrue();
        }

        @Test
        @DisplayName("GET /api/doctor/profile retrieves authenticated doctor's own profile (200 OK)")
        void shouldGetProfileSuccessfully() throws Exception {
            persistDoctor1Profile();

            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.name").value("Dr. Sarah Collins"))
                    .andExpect(jsonPath("$.email").value("dr.sarah@example.com"))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"))
                    .andExpect(jsonPath("$.clinicName").value("HeartCare Clinic"));
        }

        @Test
        @DisplayName("PUT /api/doctor/profile updates authenticated doctor's own profile (200 OK)")
        void shouldUpdateProfileSuccessfully() throws Exception {
            persistDoctor1Profile();

            DoctorProfileUpdateRequest updateRequest = DoctorProfileUpdateRequest.builder()
                    .displayName("Dr. Sarah Collins MD")
                    .specialization("Interventional Cardiology")
                    .qualifications("MBBS, MD (Cardiology), FACC, FESC")
                    .yearsOfExperience(15)
                    .clinicName("Advanced Heart Institute")
                    .clinicAddress("200 Park Avenue, New York, NY 10022")
                    .consultationFee(new BigDecimal("250.00"))
                    .bio("Updated bio with 15 years of experience.")
                    .phoneNumber("+1-555-999-7777")
                    .build();

            mockMvc.perform(put("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.displayName").value("Dr. Sarah Collins MD"))
                    .andExpect(jsonPath("$.specialization").value("Interventional Cardiology"))
                    .andExpect(jsonPath("$.yearsOfExperience").value(15))
                    .andExpect(jsonPath("$.clinicName").value("Advanced Heart Institute"))
                    .andExpect(jsonPath("$.consultationFee").value(250.00));

            DoctorProfile updated = doctorProfileRepository.findByUserId(doctorUser1.getId()).orElseThrow();
            assertThat(updated.getSpecialization()).isEqualTo("Interventional Cardiology");
            assertThat(updated.getYearsOfExperience()).isEqualTo(15);
            assertThat(updated.getClinicName()).isEqualTo("Advanced Heart Institute");
        }

        @Test
        @DisplayName("GET /api/admin/doctors/{doctorId}/profile allows admin to view a doctor profile (200 OK)")
        void adminShouldViewDoctorProfileById() throws Exception {
            DoctorProfile saved = persistDoctor1Profile();

            mockMvc.perform(get("/api/admin/doctors/" + saved.getId() + "/profile")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"))
                    .andExpect(jsonPath("$.clinicName").value("HeartCare Clinic"));
        }
    }

    // ─── Security, RBAC and Ownership Enforcement ─────────────────────────────

    @Nested
    @DisplayName("Security, RBAC and Ownership Enforcement")
    class SecurityAndOwnershipOperations {

        @Test
        @DisplayName("Unauthenticated request to POST/GET/PUT doctor profile should return 401 Unauthorized")
        void shouldRejectUnauthenticatedRequests() throws Exception {
            mockMvc.perform(post("/api/doctor/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            mockMvc.perform(get("/api/doctor/profile"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            mockMvc.perform(put("/api/doctor/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("PATIENT role is forbidden from doctor profile endpoints (403 Forbidden)")
        void shouldRejectPatientRole() throws Exception {
            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + patientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + patientToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(put("/api/doctor/profile")
                            .header("Authorization", "Bearer " + patientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("ADMIN role is forbidden from doctor self-service profile endpoints (403 Forbidden)")
        void shouldRejectAdminOnSelfServiceEndpoints() throws Exception {
            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));

            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("PATIENT role is forbidden from admin doctor profile endpoint (403 Forbidden)")
        void shouldRejectPatientOnAdminEndpoint() throws Exception {
            DoctorProfile saved = persistDoctor1Profile();

            mockMvc.perform(get("/api/admin/doctors/" + saved.getId() + "/profile")
                            .header("Authorization", "Bearer " + patientToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("DOCTOR role is forbidden from admin doctor profile endpoint (403 Forbidden)")
        void shouldRejectDoctorOnAdminEndpoint() throws Exception {
            DoctorProfile saved = persistDoctor1Profile();

            mockMvc.perform(get("/api/admin/doctors/" + saved.getId() + "/profile")
                            .header("Authorization", "Bearer " + doctor1Token))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("GET /api/doctor/profile — doctor sees only their own profile, not another doctor's")
        void shouldIsolateProfilesBetweenDoctors() throws Exception {
            persistDoctor1Profile();

            doctorProfileRepository.save(DoctorProfile.builder()
                    .user(doctorUser2)
                    .displayName("Dr. James Reed")
                    .specialization("Neurology")
                    .qualifications("MBBS, MD (Neurology)")
                    .yearsOfExperience(8)
                    .clinicName("BrainCare Center")
                    .clinicAddress("99 Neuroscience Blvd, Boston, MA")
                    .build());

            // Doctor 1 gets their own profile
            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.specialization").value("Cardiology"));

            // Doctor 2 gets their own profile
            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor2Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(doctorUser2.getId()))
                    .andExpect(jsonPath("$.specialization").value("Neurology"));
        }

        @Test
        @DisplayName("Request body with spoofed userId cannot reassign profile ownership")
        void shouldIgnoreSpoofedUserIdInRequestBody() throws Exception {
            String maliciousPayload = """
                    {
                        "userId": 999999,
                        "displayName": "Dr. Sarah Collins",
                        "specialization": "Cardiology",
                        "qualifications": "MBBS, MD",
                        "yearsOfExperience": 10,
                        "clinicName": "My Clinic",
                        "clinicAddress": "123 Fake St, Anytown, NY 10001"
                    }
                    """;

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(maliciousPayload))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.userId").value(doctorUser1.getId()))
                    .andExpect(jsonPath("$.email").value("dr.sarah@example.com"));

            DoctorProfile saved = doctorProfileRepository.findByUserId(doctorUser1.getId()).orElseThrow();
            assertThat(saved.getUser().getId()).isEqualTo(doctorUser1.getId());
        }

        @Test
        @DisplayName("Invalid or expired JWT is rejected with 401 Unauthorized")
        void shouldRejectInvalidOrExpiredJwt() throws Exception {
            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            // Expired token
            UserDetails doctor1Details = userDetailsService.loadUserByUsername("dr.sarah@example.com");
            String expiredToken = jwtService.generateTokenWithCustomExpiration(
                    doctor1Details, doctorUser1.getId(), "DOCTOR", -5000);

            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    // ─── Validation and Conflict Error Handling ────────────────────────────────

    @Nested
    @DisplayName("Validation and Conflict Error Handling")
    class ValidationAndErrorHandlingOperations {

        @Test
        @DisplayName("Duplicate profile creation returns 409 Conflict")
        void shouldRejectDuplicateProfileCreation() throws Exception {
            persistDoctor1Profile();

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCreateRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("already exists")));
        }

        @Test
        @DisplayName("GET on non-existent doctor profile returns 404 Not Found")
        void shouldReturn404WhenProfileNotFoundOnGet() throws Exception {
            mockMvc.perform(get("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message", containsString("not found")));
        }

        @Test
        @DisplayName("PUT on non-existent doctor profile returns 404 Not Found")
        void shouldReturn404WhenProfileNotFoundOnPut() throws Exception {
            DoctorProfileUpdateRequest updateRequest = DoctorProfileUpdateRequest.builder()
                    .displayName("Dr. Sarah Collins")
                    .specialization("Cardiology")
                    .qualifications("MBBS, MD")
                    .yearsOfExperience(10)
                    .clinicName("City Clinic")
                    .clinicAddress("100 Main Street, Chicago, IL 60601")
                    .build();

            mockMvc.perform(put("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("GET admin endpoint on non-existent doctor profile returns 404 Not Found")
        void adminShouldGet404ForNonExistentProfile() throws Exception {
            mockMvc.perform(get("/api/admin/doctors/99999/profile")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }

        @Test
        @DisplayName("Missing required fields on create returns 400 Bad Request with validation errors")
        void shouldRejectMissingRequiredFields() throws Exception {
            // Completely empty body — all required fields missing
            String emptyBody = """
                    {
                        "consultationFee": 100.00
                    }
                    """;

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.displayName").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.specialization").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.qualifications").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.yearsOfExperience").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.clinicName").isNotEmpty())
                    .andExpect(jsonPath("$.validationErrors.clinicAddress").isNotEmpty());
        }

        @Test
        @DisplayName("Negative yearsOfExperience is rejected with 400 Bad Request")
        void shouldRejectNegativeYearsOfExperience() throws Exception {
            DoctorProfileCreateRequest request = DoctorProfileCreateRequest.builder()
                    .displayName("Dr. Sarah Collins")
                    .specialization("Cardiology")
                    .qualifications("MBBS, MD")
                    .yearsOfExperience(-1)
                    .clinicName("HeartCare Clinic")
                    .clinicAddress("45 Medical Park, New York, NY 10001")
                    .build();

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.yearsOfExperience").isNotEmpty());
        }

        @Test
        @DisplayName("Invalid phone number format is rejected with 400 Bad Request")
        void shouldRejectInvalidPhoneNumberFormat() throws Exception {
            DoctorProfileCreateRequest request = DoctorProfileCreateRequest.builder()
                    .displayName("Dr. Sarah Collins")
                    .specialization("Cardiology")
                    .qualifications("MBBS, MD")
                    .yearsOfExperience(10)
                    .clinicName("HeartCare Clinic")
                    .clinicAddress("45 Medical Park, New York, NY 10001")
                    .phoneNumber("bad-phone!!!")
                    .build();

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.phoneNumber").isNotEmpty());
        }

        @Test
        @DisplayName("Negative consultation fee is rejected with 400 Bad Request")
        void shouldRejectNegativeConsultationFee() throws Exception {
            DoctorProfileCreateRequest request = DoctorProfileCreateRequest.builder()
                    .displayName("Dr. Sarah Collins")
                    .specialization("Cardiology")
                    .qualifications("MBBS, MD")
                    .yearsOfExperience(10)
                    .clinicName("HeartCare Clinic")
                    .clinicAddress("45 Medical Park, New York, NY 10001")
                    .consultationFee(new BigDecimal("-50.00"))
                    .build();

            mockMvc.perform(post("/api/doctor/profile")
                            .header("Authorization", "Bearer " + doctor1Token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.validationErrors.consultationFee").isNotEmpty());
        }
    }
}
