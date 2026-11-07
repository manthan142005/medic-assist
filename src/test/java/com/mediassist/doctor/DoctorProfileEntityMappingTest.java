package com.mediassist.doctor;

import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class DoctorProfileEntityMappingTest {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorProfileEntityMappingTest(DoctorProfileRepository doctorProfileRepository,
                                          UserRepository userRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("DoctorProfile entity should persist and retrieve correctly with User relationship")
    void shouldPersistAndRetrieveProfile() {
        User user = userRepository.save(User.builder()
                .name("Entity Test Doctor")
                .email("entity.doctor@example.com")
                .password("Password123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .displayName("Dr. Entity Doctor")
                .specialization("Neurology")
                .qualifications("MBBS, MD")
                .yearsOfExperience(10)
                .clinicName("Neuro Health Center")
                .clinicAddress("789 Brain Way, Suite 400")
                .consultationFee(new BigDecimal("200.00"))
                .bio("Board certified neurologist.")
                .phoneNumber("+1-555-777-8888")
                .build();

        DoctorProfile savedProfile = doctorProfileRepository.save(profile);

        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getCreatedAt()).isNotNull();
        assertThat(savedProfile.getUpdatedAt()).isNotNull();
        assertThat(savedProfile.getUser().getId()).isEqualTo(user.getId());

        Optional<DoctorProfile> retrieved = doctorProfileRepository.findByUserId(user.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDisplayName()).isEqualTo("Dr. Entity Doctor");
        assertThat(retrieved.get().getSpecialization()).isEqualTo("Neurology");
        assertThat(retrieved.get().getQualifications()).isEqualTo("MBBS, MD");
        assertThat(retrieved.get().getYearsOfExperience()).isEqualTo(10);
        assertThat(retrieved.get().getClinicName()).isEqualTo("Neuro Health Center");
        assertThat(retrieved.get().getClinicAddress()).isEqualTo("789 Brain Way, Suite 400");
        assertThat(retrieved.get().getConsultationFee()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(retrieved.get().getBio()).isEqualTo("Board certified neurologist.");
        assertThat(retrieved.get().getPhoneNumber()).isEqualTo("+1-555-777-8888");
    }

    @Test
    @DisplayName("Enforce unique constraint on user_id in doctor_profiles")
    void shouldEnforceUniqueConstraintOnUserId() {
        User user = userRepository.save(User.builder()
                .name("Duplicate Test Doctor")
                .email("duplicate.doctor@example.com")
                .password("Password123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build());

        DoctorProfile profile1 = DoctorProfile.builder()
                .user(user)
                .displayName("Dr. First Profile")
                .specialization("Dermatology")
                .qualifications("MBBS")
                .yearsOfExperience(5)
                .clinicName("Skin Clinic")
                .clinicAddress("100 Skin Ave")
                .build();
        doctorProfileRepository.saveAndFlush(profile1);

        DoctorProfile profile2 = DoctorProfile.builder()
                .user(user)
                .displayName("Dr. Second Profile")
                .specialization("Pediatrics")
                .qualifications("MBBS, MD")
                .yearsOfExperience(8)
                .clinicName("Kids Clinic")
                .clinicAddress("200 Kids Ave")
                .build();

        assertThatThrownBy(() -> doctorProfileRepository.saveAndFlush(profile2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

