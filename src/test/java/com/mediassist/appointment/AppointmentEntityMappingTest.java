package com.mediassist.appointment;

import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.DoctorProfile;
import com.mediassist.doctor.DoctorProfileRepository;
import com.mediassist.patient.Gender;
import com.mediassist.patient.PatientProfile;
import com.mediassist.patient.PatientProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AppointmentEntityMappingTest {

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentEntityMappingTest(AppointmentRepository appointmentRepository,
                                        PatientProfileRepository patientProfileRepository,
                                        DoctorProfileRepository doctorProfileRepository,
                                        UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("Appointment entity should persist and retrieve correctly with Patient and Doctor relationships")
    void shouldPersistAndRetrieveAppointment() {
        User patientUser = userRepository.save(User.builder()
                .name("Entity Patient")
                .email("entity.patient.apt@example.com")
                .password("Password123!")
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());

        PatientProfile patient = patientProfileRepository.save(PatientProfile.builder()
                .user(patientUser)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender(Gender.MALE)
                .phoneNumber("+1-555-123-4567")
                .address("100 Patient Rd")
                .build());

        User doctorUser = userRepository.save(User.builder()
                .name("Entity Doctor")
                .email("entity.doctor.apt@example.com")
                .password("Password123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());

        DoctorProfile doctor = doctorProfileRepository.save(DoctorProfile.builder()
                .user(doctorUser)
                .displayName("Dr. Entity")
                .specialization("General Practice")
                .qualifications("MBBS")
                .yearsOfExperience(8)
                .clinicName("Wellness Clinic")
                .clinicAddress("200 Health Ave")
                .consultationFee(new BigDecimal("100.00"))
                .build());

        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime start = LocalTime.of(14, 0);
        LocalTime end = LocalTime.of(14, 30);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(date)
                .startTime(start)
                .endTime(end)
                .reason("Routine checkup")
                .notes("No special symptoms")
                .status(AppointmentStatus.REQUESTED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.REQUESTED);

        Optional<Appointment> retrieved = appointmentRepository.findByIdWithDetails(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getPatient().getId()).isEqualTo(patient.getId());
        assertThat(retrieved.get().getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(retrieved.get().getAppointmentDate()).isEqualTo(date);
        assertThat(retrieved.get().getStartTime()).isEqualTo(start);
        assertThat(retrieved.get().getEndTime()).isEqualTo(end);
        assertThat(retrieved.get().getReason()).isEqualTo("Routine checkup");
    }
}

