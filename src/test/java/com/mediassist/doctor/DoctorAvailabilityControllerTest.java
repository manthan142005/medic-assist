package com.mediassist.doctor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mediassist.appointment.Appointment;
import com.mediassist.appointment.AppointmentRepository;
import com.mediassist.appointment.AppointmentStatus;
import com.mediassist.appointment.dto.AppointmentBookingRequest;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.dto.DoctorAvailabilityRequest;
import com.mediassist.doctor.dto.DoctorLeaveRequest;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DoctorAvailabilityControllerTest {

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorLeaveRepository leaveRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private User doctorUser1;
    private DoctorProfile doctorProfile1;
    private String doctorToken1;

    private User doctorUser2;
    private DoctorProfile doctorProfile2;
    private String doctorToken2;

    private User patientUser;
    private PatientProfile patientProfile;
    private String patientToken;

    @Autowired
    public DoctorAvailabilityControllerTest(MockMvc mockMvc,
                                            UserRepository userRepository,
                                            DoctorProfileRepository doctorProfileRepository,
                                            PatientProfileRepository patientProfileRepository,
                                            DoctorAvailabilityRepository availabilityRepository,
                                            DoctorLeaveRepository leaveRepository,
                                            AppointmentRepository appointmentRepository,
                                            PasswordEncoder passwordEncoder,
                                            JwtService jwtService,
                                            CustomUserDetailsService userDetailsService) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.availabilityRepository = availabilityRepository;
        this.leaveRepository = leaveRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        // Doctor 1
        doctorUser1 = userRepository.save(User.builder()
                .name("Dr. Marcus Vance")
                .email("marcus.vance@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        doctorProfile1 = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser1)
                .displayName("Dr. Marcus Vance MD")
                .specialization("General Medicine")
                .qualifications("MBBS, MD")
                .yearsOfExperience(15)
                .clinicName("Vance Medical")
                .clinicAddress("100 Primary Way")
                .consultationFee(new BigDecimal("120.00"))
                .build());
        UserDetails d1Details = userDetailsService.loadUserByUsername("marcus.vance@example.com");
        doctorToken1 = jwtService.generateToken(d1Details, doctorUser1.getId(), "DOCTOR");

        // Doctor 2
        doctorUser2 = userRepository.save(User.builder()
                .name("Dr. Helen Cho")
                .email("helen.cho@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());
        doctorProfile2 = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser2)
                .displayName("Dr. Helen Cho MD")
                .specialization("Dermatology")
                .qualifications("MBBS, DVD")
                .yearsOfExperience(8)
                .clinicName("Skin Care Center")
                .clinicAddress("200 Derma Blvd")
                .consultationFee(new BigDecimal("160.00"))
                .build());
        UserDetails d2Details = userDetailsService.loadUserByUsername("helen.cho@example.com");
        doctorToken2 = jwtService.generateToken(d2Details, doctorUser2.getId(), "DOCTOR");

        // Patient
        patientUser = userRepository.save(User.builder()
                .name("Sam Patient")
                .email("sam.avail.patient@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());
        patientProfile = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser)
                .dateOfBirth(LocalDate.of(1995, 4, 10))
                .gender(Gender.MALE)
                .phoneNumber("+1-555-444-5555")
                .address("500 Maple Ave")
                .build());
        UserDetails pDetails = userDetailsService.loadUserByUsername("sam.avail.patient@example.com");
        patientToken = jwtService.generateToken(pDetails, patientUser.getId(), "PATIENT");
    }

    // ─── Doctor Availability Schedule Tests ───────────────────────────────────

    @Nested
    @DisplayName("Doctor Availability Schedule Operations")
    class AvailabilityScheduleTests {

        @Test
        @DisplayName("POST /api/doctor/availability — Doctor creates working session (201 Created)")
        void shouldCreateAvailabilitySession() throws Exception {
            DoctorAvailabilityRequest request = DoctorAvailabilityRequest.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .slotDurationMinutes(30)
                    .build();

            mockMvc.perform(post("/api/doctor/availability")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.doctorId").value(doctorProfile1.getId()))
                    .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                    .andExpect(jsonPath("$.startTime").value("09:00:00"))
                    .andExpect(jsonPath("$.endTime").value("12:00:00"))
                    .andExpect(jsonPath("$.slotDurationMinutes").value(30));

            assertThat(availabilityRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("GET /api/doctor/availability — Doctor retrieves own schedule")
        void shouldRetrieveDoctorAvailabilitySchedule() throws Exception {
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .slotDurationMinutes(30)
                    .build());
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(13, 0))
                    .endTime(LocalTime.of(17, 0))
                    .slotDurationMinutes(30)
                    .build());

            mockMvc.perform(get("/api/doctor/availability")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].startTime").value("09:00:00"))
                    .andExpect(jsonPath("$[1].startTime").value("13:00:00"));
        }

        @Test
        @DisplayName("PUT /api/doctor/availability — Doctor updates full weekly schedule")
        void shouldUpdateFullWeeklySchedule() throws Exception {
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .build());

            List<DoctorAvailabilityRequest> newSchedule = List.of(
                    DoctorAvailabilityRequest.builder()
                            .dayOfWeek(DayOfWeek.TUESDAY)
                            .startTime(LocalTime.of(10, 0))
                            .endTime(LocalTime.of(13, 0))
                            .slotDurationMinutes(30)
                            .build(),
                    DoctorAvailabilityRequest.builder()
                            .dayOfWeek(DayOfWeek.TUESDAY)
                            .startTime(LocalTime.of(14, 0))
                            .endTime(LocalTime.of(18, 0))
                            .slotDurationMinutes(30)
                            .build()
            );

            mockMvc.perform(put("/api/doctor/availability")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newSchedule)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].dayOfWeek").value("TUESDAY"));

            List<DoctorAvailability> current = availabilityRepository.findAllByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorProfile1.getId());
            assertThat(current).hasSize(2);
            assertThat(current.get(0).getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        }

        @Test
        @DisplayName("DELETE /api/doctor/availability/{id} — Doctor deletes specific availability session")
        void shouldDeleteAvailabilitySession() throws Exception {
            DoctorAvailability saved = availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build());

            mockMvc.perform(delete("/api/doctor/availability/" + saved.getId())
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk());

            assertThat(availabilityRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/doctor/availability/{id} — Doctor cannot delete another doctor's session (403 Forbidden)")
        void doctorCannotDeleteOtherDoctorSession() throws Exception {
            DoctorAvailability saved = availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile2)
                    .dayOfWeek(DayOfWeek.WEDNESDAY)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build());

            mockMvc.perform(delete("/api/doctor/availability/" + saved.getId())
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Overlapping sessions on the same day are rejected with 409 Conflict")
        void shouldRejectOverlappingSessionsOnSameDay() throws Exception {
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .build());

            // Overlapping session 11:00 - 14:00 on MONDAY
            DoctorAvailabilityRequest overlapping = DoctorAvailabilityRequest.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build();

            mockMvc.perform(post("/api/doctor/availability")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(overlapping)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("overlaps with an existing session")));
        }

        @Test
        @DisplayName("End time before or equal to start time is rejected with 400 Bad Request")
        void shouldRejectInvalidSessionTime() throws Exception {
            DoctorAvailabilityRequest invalid = DoctorAvailabilityRequest.builder()
                    .dayOfWeek(DayOfWeek.FRIDAY)
                    .startTime(LocalTime.of(15, 0))
                    .endTime(LocalTime.of(14, 0))
                    .build();

            mockMvc.perform(post("/api/doctor/availability")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("after start time")));
        }
    }

    // ─── Doctor Leave Operations Tests ────────────────────────────────────────

    @Nested
    @DisplayName("Doctor Leave Operations")
    class DoctorLeaveTests {

        @Test
        @DisplayName("POST /api/doctor/leave — Doctor creates leave record (201 Created)")
        void shouldCreateDoctorLeave() throws Exception {
            LocalDate start = LocalDate.now().plusDays(10);
            LocalDate end = LocalDate.now().plusDays(14);

            DoctorLeaveRequest request = DoctorLeaveRequest.builder()
                    .startDate(start)
                    .endDate(end)
                    .reason("Annual vacation")
                    .build();

            mockMvc.perform(post("/api/doctor/leave")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.doctorId").value(doctorProfile1.getId()))
                    .andExpect(jsonPath("$.reason").value("Annual vacation"));

            assertThat(leaveRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("GET /api/doctor/leave — Doctor retrieves leave records")
        void shouldRetrieveDoctorLeaveRecords() throws Exception {
            LocalDate start = LocalDate.now().plusDays(5);
            LocalDate end = LocalDate.now().plusDays(8);

            leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile1)
                    .startDate(start)
                    .endDate(end)
                    .reason("Personal leave")
                    .build());

            mockMvc.perform(get("/api/doctor/leave")
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].reason").value("Personal leave"));
        }

        @Test
        @DisplayName("DELETE /api/doctor/leave/{id} — Doctor cancels future leave")
        void shouldCancelFutureLeave() throws Exception {
            LocalDate start = LocalDate.now().plusDays(15);
            LocalDate end = LocalDate.now().plusDays(20);

            DoctorLeave saved = leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile1)
                    .startDate(start)
                    .endDate(end)
                    .build());

            mockMvc.perform(delete("/api/doctor/leave/" + saved.getId())
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isOk());

            assertThat(leaveRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("DELETE /api/doctor/leave/{id} — Doctor cannot cancel another doctor's leave (403 Forbidden)")
        void doctorCannotCancelOtherDoctorLeave() throws Exception {
            LocalDate start = LocalDate.now().plusDays(15);
            LocalDate end = LocalDate.now().plusDays(20);

            DoctorLeave saved = leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile2)
                    .startDate(start)
                    .endDate(end)
                    .build());

            mockMvc.perform(delete("/api/doctor/leave/" + saved.getId())
                            .header("Authorization", "Bearer " + doctorToken1))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Overlapping leave periods are rejected with 409 Conflict")
        void shouldRejectOverlappingLeavePeriods() throws Exception {
            LocalDate start = LocalDate.now().plusDays(5);
            LocalDate end = LocalDate.now().plusDays(10);

            leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile1)
                    .startDate(start)
                    .endDate(end)
                    .build());

            // Overlapping leave (day 8 to 12)
            DoctorLeaveRequest overlapping = DoctorLeaveRequest.builder()
                    .startDate(LocalDate.now().plusDays(8))
                    .endDate(LocalDate.now().plusDays(12))
                    .reason("Overlapping trip")
                    .build();

            mockMvc.perform(post("/api/doctor/leave")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(overlapping)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("already has leave scheduled")));
        }

        @Test
        @DisplayName("Leave cannot be created if doctor already has ACCEPTED appointments in that period (409 Conflict)")
        void shouldRejectLeaveWhenAcceptedAppointmentsExist() throws Exception {
            LocalDate leaveStart = LocalDate.now().plusDays(5);
            LocalDate leaveEnd = LocalDate.now().plusDays(10);

            // Existing accepted appointment within leave period
            appointmentRepository.save(Appointment.builder()
                    .patient(patientProfile)
                    .doctor(doctorProfile1)
                    .appointmentDate(LocalDate.now().plusDays(7))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .reason("Existing consultation")
                    .status(AppointmentStatus.ACCEPTED)
                    .build());

            DoctorLeaveRequest request = DoctorLeaveRequest.builder()
                    .startDate(leaveStart)
                    .endDate(leaveEnd)
                    .reason("Sudden leave")
                    .build();

            mockMvc.perform(post("/api/doctor/leave")
                            .header("Authorization", "Bearer " + doctorToken1)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message", containsString("accepted appointments")));
        }
    }

    // ─── Available Slots Generation Tests ─────────────────────────────────────

    @Nested
    @DisplayName("Available Slots Generation")
    class AvailableSlotTests {

        @Test
        @DisplayName("GET /api/doctors/{id}/available-slots generates slots, respects breaks and exclusions")
        void shouldGenerateSlotsRespectingBreaksAndAppointments() throws Exception {
            // Find next Monday
            LocalDate nextMonday = LocalDate.now().plusDays(1);
            while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
                nextMonday = nextMonday.plusDays(1);
            }

            // Morning session: 09:00 - 11:00 (4 slots of 30 min: 09:00, 09:30, 10:00, 10:30)
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(11, 0))
                    .slotDurationMinutes(30)
                    .build());

            // Afternoon session: 13:00 - 15:00 (4 slots of 30 min: 13:00, 13:30, 14:00, 14:30)
            // Break is between 11:00 and 13:00
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(13, 0))
                    .endTime(LocalTime.of(15, 0))
                    .slotDurationMinutes(30)
                    .build());

            // Existing occupied appointment at 09:30 - 10:00
            appointmentRepository.save(Appointment.builder()
                    .patient(patientProfile)
                    .doctor(doctorProfile1)
                    .appointmentDate(nextMonday)
                    .startTime(LocalTime.of(9, 30))
                    .endTime(LocalTime.of(10, 0))
                    .reason("Booked slot")
                    .status(AppointmentStatus.REQUESTED)
                    .build());

            // Request available slots
            mockMvc.perform(get("/api/doctors/" + doctorProfile1.getId() + "/available-slots")
                            .param("date", nextMonday.toString())
                            .header("Authorization", "Bearer " + patientToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.doctorId").value(doctorProfile1.getId()))
                    .andExpect(jsonPath("$.date").value(nextMonday.toString()))
                    // 4 morning - 1 booked + 4 afternoon = 7 available slots total
                    .andExpect(jsonPath("$.availableSlots", hasSize(7)))
                    // First slot 09:00 - 09:30 is available
                    .andExpect(jsonPath("$.availableSlots[0].startTime").value("09:00:00"))
                    .andExpect(jsonPath("$.availableSlots[0].endTime").value("09:30:00"))
                    // Slot 09:30 is skipped because it's booked, so next is 10:00
                    .andExpect(jsonPath("$.availableSlots[1].startTime").value("10:00:00"))
                    // Break period (11:00 - 13:00) has no slots; next starts at 13:00
                    .andExpect(jsonPath("$.availableSlots[3].startTime").value("13:00:00"));
        }

        @Test
        @DisplayName("Leave dates return empty available slots array")
        void leaveDateReturnsEmptySlots() throws Exception {
            LocalDate nextMonday = LocalDate.now().plusDays(1);
            while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
                nextMonday = nextMonday.plusDays(1);
            }

            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .build());

            // Doctor takes leave on next Monday
            leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile1)
                    .startDate(nextMonday)
                    .endDate(nextMonday)
                    .reason("Conference")
                    .build());

            mockMvc.perform(get("/api/doctors/" + doctorProfile1.getId() + "/available-slots")
                            .param("date", nextMonday.toString())
                            .header("Authorization", "Bearer " + patientToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.availableSlots", hasSize(0)));
        }

        @Test
        @DisplayName("Doctor without any configured schedule returns 404 Not Found")
        void doctorWithoutScheduleReturns404() throws Exception {
            LocalDate futureDate = LocalDate.now().plusDays(2);

            mockMvc.perform(get("/api/doctors/" + doctorProfile2.getId() + "/available-slots")
                            .param("date", futureDate.toString())
                            .header("Authorization", "Bearer " + patientToken))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message", containsString("not configured")));
        }
    }

    // ─── Booking Integration with Working Hours & Leave ───────────────────────

    @Nested
    @DisplayName("Booking Integration with Working Hours & Leave")
    class BookingIntegrationTests {

        @Test
        @DisplayName("Appointment booking outside working hours is rejected with 400 Bad Request")
        void bookingOutsideWorkingHoursIsRejected() throws Exception {
            LocalDate nextMonday = LocalDate.now().plusDays(1);
            while (nextMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
                nextMonday = nextMonday.plusDays(1);
            }

            // Working hours: 09:00 - 12:00
            availabilityRepository.save(DoctorAvailability.builder()
                    .doctor(doctorProfile1)
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .build());

            // Patient attempts booking at 14:00 - 14:30 (outside working schedule)
            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(nextMonday)
                    .startTime(LocalTime.of(14, 0))
                    .endTime(LocalTime.of(14, 30))
                    .reason("Afternoon visit")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("outside the doctor's working schedule")));
        }

        @Test
        @DisplayName("Appointment booking during doctor leave is rejected with 400 Bad Request")
        void bookingDuringLeaveIsRejected() throws Exception {
            LocalDate leaveDate = LocalDate.now().plusDays(7);

            leaveRepository.save(DoctorLeave.builder()
                    .doctor(doctorProfile1)
                    .startDate(leaveDate)
                    .endDate(leaveDate)
                    .reason("Doctor vacation")
                    .build());

            AppointmentBookingRequest request = AppointmentBookingRequest.builder()
                    .doctorId(doctorProfile1.getId())
                    .appointmentDate(leaveDate)
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(10, 30))
                    .reason("Booking during leave")
                    .build();

            mockMvc.perform(post("/api/appointments")
                            .header("Authorization", "Bearer " + patientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message", containsString("on leave")));
        }
    }

    // ─── Security & RBAC Tests ────────────────────────────────────────────────

    @Nested
    @DisplayName("Security & RBAC Enforcement")
    class SecurityTests {

        @Test
        @DisplayName("Unauthenticated request to doctor availability returns 401 Unauthorized")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/doctor/availability"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }

        @Test
        @DisplayName("Patient role is forbidden from modifying doctor availability (403 Forbidden)")
        void patientRoleCannotModifyAvailability() throws Exception {
            DoctorAvailabilityRequest request = DoctorAvailabilityRequest.builder()
                    .dayOfWeek(DayOfWeek.MONDAY)
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(12, 0))
                    .build();

            mockMvc.perform(post("/api/doctor/availability")
                            .header("Authorization", "Bearer " + patientToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403));
        }
    }
}

