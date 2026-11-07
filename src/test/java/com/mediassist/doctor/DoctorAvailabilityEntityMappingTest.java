package com.mediassist.doctor;

import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DoctorAvailabilityEntityMappingTest {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorLeaveRepository leaveRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorAvailabilityEntityMappingTest(DoctorAvailabilityRepository availabilityRepository,
                                               DoctorLeaveRepository leaveRepository,
                                               DoctorProfileRepository doctorProfileRepository,
                                               UserRepository userRepository) {
        this.availabilityRepository = availabilityRepository;
        this.leaveRepository = leaveRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("DoctorAvailability entity should persist and retrieve correctly")
    void shouldPersistAndRetrieveDoctorAvailability() {
        User doctorUser = userRepository.save(User.builder()
                .name("Entity Test Doctor Avail")
                .email("entity.avail.doc@example.com")
                .password("Password123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());

        DoctorProfile doctor = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser)
                .displayName("Dr. Avail Test")
                .specialization("Pediatrics")
                .qualifications("MBBS, MD")
                .yearsOfExperience(7)
                .clinicName("Kids Wellness")
                .clinicAddress("123 Child St")
                .consultationFee(new BigDecimal("120.00"))
                .build());

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .isActive(true)
                .build();

        DoctorAvailability saved = availabilityRepository.save(availability);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);

        Optional<DoctorAvailability> retrieved = availabilityRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getStartTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(retrieved.get().getEndTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(retrieved.get().getSlotDurationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("DoctorLeave entity should persist and retrieve correctly")
    void shouldPersistAndRetrieveDoctorLeave() {
        User doctorUser = userRepository.save(User.builder()
                .name("Entity Test Doctor Leave")
                .email("entity.leave.doc@example.com")
                .password("Password123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());

        DoctorProfile doctor = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser)
                .displayName("Dr. Leave Test")
                .specialization("Orthopedics")
                .qualifications("MBBS, MS")
                .yearsOfExperience(9)
                .clinicName("Bone Clinic")
                .clinicAddress("456 Joint Ave")
                .consultationFee(new BigDecimal("140.00"))
                .build());

        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = LocalDate.now().plusDays(15);

        DoctorLeave leave = DoctorLeave.builder()
                .doctor(doctor)
                .startDate(start)
                .endDate(end)
                .reason("Medical conference")
                .build();

        DoctorLeave saved = leaveRepository.save(leave);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getStartDate()).isEqualTo(start);
        assertThat(saved.getEndDate()).isEqualTo(end);
        assertThat(saved.getReason()).isEqualTo("Medical conference");

        Optional<DoctorLeave> retrieved = leaveRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDoctor().getId()).isEqualTo(doctor.getId());
    }
}

