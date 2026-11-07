package com.mediassist.appointment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediassist.appointment.dto.AppointmentBookingRequest;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.DoctorProfile;
import com.mediassist.doctor.DoctorProfileRepository;
import com.mediassist.patient.Gender;
import com.mediassist.patient.PatientProfile;
import com.mediassist.patient.PatientProfileRepository;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AppointmentControllerTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private User patientUser1;
    private PatientProfile patientProfile1;
    private String patientToken1;

    private User patientUser2;
    private PatientProfile patientProfile2;
    private String patientToken2;

    private User doctorUser1;
    private DoctorProfile doctorProfile1;
    private String doctorToken1;

    private User doctorUser2;
    private DoctorProfile doctorProfile2;
    private String doctorToken2;

    private User adminUser;
    private String adminToken;

    @Autowired
    public AppointmentControllerTest(MockMvc mockMvc,
                                     UserRepository userRepository,
                                     PatientProfileRepository patientProfileRepository,
                                     DoctorProfileRepository doctorProfileRepository,
                                     AppointmentRepository appointmentRepository,
                                     PasswordEncoder passwordEncoder,
                                     JwtService jwtService,
                                     CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        // Patient 1
        patientUser1 = userRepository.save(User.builder()
                .name("John Doe")
                .email("john.apt@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        patientProfile1 = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser1)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .phoneNumber("+1-555-111-2222")
                .address("100 Elm Street")
                .build());
        UserDetails p1Details = userDetailsService.loadUserByUsername("john.apt@example.com");
        patientToken1 = jwtService.generateToken(p1Details, patientUser1.getId(), "PATIENT");

        // Patient 2
        patientUser2 = userRepository.save(User.builder()
                .name("Jane Smith")
                .email("jane.apt@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        patientProfile2 = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser2)
                .dateOfBirth(LocalDate.of(1992, 2, 2))
                .gender(Gender.FEMALE)
                .phoneNumber("+1-555-333-4444")
                .address("200 Oak Street")
                .build());
        UserDetails p2Details = userDetailsService.loadUserByUsername("jane.apt@example.com");
        patientToken2 = jwtService.generateToken(p2Details, patientUser2.getId(), "PATIENT");

        // Doctor 1
        doctorUser1 = userRepository.save(User.builder()
                .name("Dr. Alice Johnson")
                .email("dr.alice.apt@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        doctorProfile1 = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser1)
                .displayName("Dr. Alice Johnson MD")
                .specialization("Cardiology")
                .qualifications("MBBS, MD")
                .yearsOfExperience(10)
                .clinicName("City Heart Clinic")
                .clinicAddress("300 Medical Way")
                .consultationFee(new BigDecimal("150.00"))
                .build());
        UserDetails d1Details = userDetailsService.loadUserByUsername("dr.alice.apt@example.com");
        doctorToken1 = jwtService.generateToken(d1Details, doctorUser1.getId(), "DOCTOR");

        // Doctor 2
        doctorUser2 = userRepository.save(User.builder()
                .name("Dr. Bob Wilson")
                .email("dr.bob.apt@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        doctorProfile2 = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser2)
                .displayName("Dr. Bob Wilson MD")
                .specialization("Neurology")
                .qualifications("MBBS, MD")
                .yearsOfExperience(12)
                .clinicName("Neuro Care Clinic")
                .clinicAddress("400 Brain Blvd")
                .consultationFee(new BigDecimal("180.00"))
                .build());
        UserDetails d2Details = userDetailsService.loadUserByUsername("dr.bob.apt@example.com");
        doctorToken2 = jwtService.generateToken(d2Details, doctorUser2.getId(), "DOCTOR");

        // Admin
        adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin.apt@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build());
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin.apt@example.com");
        adminToken = jwtService.generateToken(adminDetails, adminUser.getId(), "ADMIN");
    }

    private Appointment createPersistedAppointment(PatientProfile patient, DoctorProfile doctor,
                                                   LocalDate date, LocalTime start, LocalTime end,
                                                   AppointmentStatus status) {
        return appointmentRepository.save(Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(start)
                .endTime(end)
                .reason("Consultation")
                .notes("General evaluation")
                .status(status)
                .build());
    }

    // ─── Patient Booking & Lifecycle Tests ─────────────────────────────────────

    @Nested
    @DisplayName("Patient Booking & Lifecycle Operations")
    class PatientBookingOperations {

        @Test
        @DisplayName("POST /api/appointments — Patient can book an appointment (201 Created)")
        void patientShouldBookAppointmentSuccessfully() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(3);
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .reason("Annual cardiac checkup")
                    .notes("First visit")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.patientId").value(patientProfile1.getId()))
                    .andExpect(jsonPath("$.patientName").value("John Doe"))
                    .andExpect(jsonPath("$.doctorId").value(doctorProfile1.getId()))
                    .andExpect(jsonPath("$.doctorDisplayName").value("Dr. Alice Johnson MD"))
                    .andExpect(jsonPath("$.doctorSpecialization").value("Cardiology"))
                    .andExpect(jsonPath("$.status").value("REQUESTED"))
                    .andExpect(jsonPath("$.startTime").value("10:00:00"))
                    .andExpect(jsonPath("$.endTime").value("10:30:00"));

            assertThat(appointmentRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("POST /api/appointments — Booking defaults endTime to +30 minutes when endTime omitted")
        void patientShouldBookWithDefaultDuration() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(5);
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(14, 0))
                    .reason("Checkup")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.startTime").value("14:00:00"))
                    .andExpect(jsonPath("$.endTime").value("14:30:00"));
        }

        @Test
        @DisplayName("GET /api/appointments/my — Patient views only their own appointments")
        void patientShouldViewTheirOwnAppointments() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);
            createPersistedAppointment(patientProfile2, doctorProfile1, futureDate,
                    LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(get("/api/appointments/my")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].patientId").value(patientProfile1.getId()));
        }

        @Test
        @DisplayName("GET /api/appointments/{id} — Patient views their own appointment details (200 OK)")
        void patientShouldViewOwnAppointmentById() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(get("/api/appointments/" + apt.getId())
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(apt.getId()))
                    .andExpect(jsonPath("$.patientId").value(patientProfile1.getId()));
        }

        @Test
        @DisplayName("GET /api/appointments/{id} — Patient cannot view another patient's appointment (403 Forbidden)")
        void patientCannotViewAnotherPatientAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile2, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(get("/api/appointments/" + apt.getId())
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("PATCH /api/appointments/{id}/cancel — Patient cancels their own requested appointment (200 OK)")
        void patientShouldCancelOwnAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/appointments/" + apt.getId() + "/cancel")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("CANCELLED"));

            Appointment updated = appointmentRepository.findById(apt.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("PATCH /api/appointments/{id}/cancel — Patient cannot cancel another patient's appointment (403 Forbidden)")
        void patientCannotCancelAnotherPatientAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile2, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/appointments/" + apt.getId() + "/cancel")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("Cancellation policy — Cannot cancel completed or already cancelled appointment (400 Bad Request)")
        void cannotCancelCompletedOrCancelledAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment completedApt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.COMPLETED);
            Appointment cancelledApt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.CANCELLED);

            mockMvc.perform(patch("/api/appointments/" + completedApt.getId() + "/cancel")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("Cannot cancel")));

            mockMvc.perform(patch("/api/appointments/" + cancelledApt.getId() + "/cancel")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    // ─── Doctor Operations Tests ──────────────────────────────────────────────

    @Nested
    @DisplayName("Doctor Operations")
    class DoctorOperations {

        @Test
        @DisplayName("GET /api/doctor/appointments — Doctor views assigned appointments")
        void doctorShouldViewAssignedAppointments() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);
            createPersistedAppointment(patientProfile2, doctorProfile2, futureDate,
                    LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(get("/api/doctor/appointments")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].doctorId").value(doctorProfile1.getId()));
        }

        @Test
        @DisplayName("PATCH /api/doctor/appointments/{id}/accept — Doctor accepts requested appointment (200 OK)")
        void doctorShouldAcceptAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/accept")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ACCEPTED"));

            Appointment updated = appointmentRepository.findById(apt.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.ACCEPTED);
        }

        @Test
        @DisplayName("PATCH /api/doctor/appointments/{id}/reject — Doctor rejects requested appointment (200 OK)")
        void doctorShouldRejectAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/reject")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("REJECTED"));

            Appointment updated = appointmentRepository.findById(apt.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.REJECTED);
        }

        @Test
        @DisplayName("PATCH /api/doctor/appointments/{id}/complete — Doctor completes accepted appointment (200 OK)")
        void doctorShouldCompleteAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.ACCEPTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/complete")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));

            Appointment updated = appointmentRepository.findById(apt.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Doctor cannot manage another doctor's appointments (403 Forbidden)")
        void doctorCannotManageOtherDoctorAppointments() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile2, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/accept")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isForbidden());

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/reject")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Doctor cannot complete appointment that is not ACCEPTED (400 Bad Request)")
        void doctorCannotCompleteRequestedAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/complete")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("Doctor cannot accept appointment that is not REQUESTED (400 Bad Request)")
        void doctorCannotAcceptAlreadyCompletedOrCancelled() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            Appointment apt = createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.COMPLETED);

            mockMvc.perform(patch("/api/doctor/appointments/" + apt.getId() + "/accept")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    // ─── Scheduling, Conflicts & Overlaps ──────────────────────────────────────

    @Nested
    @DisplayName("Scheduling, Conflict & Overlap Prevention")
    class SchedulingConflictOperations {

        @Test
        @DisplayName("Past appointments are rejected (400 Bad Request)")
        void pastAppointmentsAreRejected() throws Exception {
            LocalDate pastDate = LocalDate.now().minusDays(1);
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(pastDate)
                    .startTime(LocalTime.of(10, 0))
                    .reason("Past checkup")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("past")));
        }

        @Test
        @DisplayName("End time before start time is rejected (400 Bad Request)")
        void endTimeBeforeStartTimeIsRejected() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(10, 0))
                    .reason("Invalid interval")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("after start time")));
        }

        @Test
        @DisplayName("Doctor overlapping booking is rejected (409 Conflict)")
        void doctorOverlappingBookingIsRejected() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(3);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.REQUESTED);

            // Patient 2 attempts overlapping booking for Doctor 1 (10:15 - 10:45)
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(10, 15))
                    .endTime(LocalTime.of(10, 45))
                    .reason("Overlapping booking attempt")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("Doctor already has an appointment")));
        }

        @Test
        @DisplayName("Patient overlapping booking with different doctor is rejected (409 Conflict)")
        void patientOverlappingBookingIsRejected() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(3);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(10, 0), LocalTime.of(10, 30), AppointmentStatus.REQUESTED);

            // Patient 1 attempts booking with Doctor 2 at the same time
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile2.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(10, 15))
                    .endTime(LocalTime.of(10, 45))
                    .reason("Patient double booking")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("You already have an appointment")));
        }

        @Test
        @DisplayName("Cancelled and Rejected appointments do NOT block time slot (201 Created)")
        void cancelledAndRejectedDoNotBlockSlot() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(4);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(11, 0), LocalTime.of(11, 30), AppointmentStatus.CANCELLED);
            createPersistedAppointment(patientProfile1, doctorProfile2, futureDate,
                    LocalTime.of(11, 0), LocalTime.of(11, 30), AppointmentStatus.REJECTED);

            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(futureDate)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(11, 30))
                    .reason("Re-booking previously cancelled slot")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken2)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("REQUESTED"));
        }

        @Test
        @DisplayName("Prevent conflicting booking from being accepted when another accepted appointment exists (409 Conflict)")
        void preventAcceptingConflictingAppointment() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(4);
            // Already accepted appointment
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(15, 0), LocalTime.of(15, 30), AppointmentStatus.ACCEPTED);

            // Another requested appointment overlapping with the accepted one
            Appointment conflictingRequested = createPersistedAppointment(patientProfile2, doctorProfile1, futureDate,
                    LocalTime.of(15, 15), LocalTime.of(15, 45), AppointmentStatus.REQUESTED);

            mockMvc.perform(patch("/api/doctor/appointments/" + conflictingRequested.getId() + "/accept")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("conflicting accepted appointment")));
        }
    }

    // ─── Security & RBAC Enforcement ──────────────────────────────────────────

    @Nested
    @DisplayName("Security & RBAC Enforcement")
    class SecurityOperations {

        @Test
        @DisplayName("Unauthenticated request returns 401 Unauthorized")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/appointments/my"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));

            mockMvc.perform(get("/api/doctor/appointments"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Invalid JWT returns 401 Unauthorized")
        void invalidJwtReturns401() throws Exception {
            mockMvc.perform(get("/api/appointments/my")
                            .header("Authorization", "Bearer invalid.token.value"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("DOCTOR role cannot book appointments via patient endpoint (403 Forbidden)")
        void doctorCannotBookAppointments() throws Exception {
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(10, 0))
                    .reason("Doctor trying to book")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("PATIENT role cannot access doctor endpoints (403 Forbidden)")
        void patientCannotAccessDoctorEndpoints() throws Exception {
            mockMvc.perform(get("/api/doctor/appointments")
                            .header("Authorization", "Bearer " + patientToken1))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }

        @Test
        @DisplayName("ADMIN can view all appointments via admin endpoint (200 OK)")
        void adminCanViewAllAppointments() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);
            createPersistedAppointment(patientProfile1, doctorProfile1, futureDate,
                    LocalTime.of(9, 0), LocalTime.of(9, 30), AppointmentStatus.REQUESTED);

            mockMvc.perform(get("/api/admin/appointments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }
}

