package com.mediassist.patient;

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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PatientProfileEntityMappingTest {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public PatientProfileEntityMappingTest(PatientProfileRepository patientProfileRepository,
                                           UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("PatientProfile entity should persist and retrieve correctly with User relationship")
    void shouldPersistAndRetrieveProfile() {
        User user = userRepository.save(User.builder()
                .name("Entity Test Patient")
                .email("entity.patient@example.com")
                .password("Password123!")
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1993, 7, 12))
                .gender(Gender.FEMALE)
                .phoneNumber("+1-555-444-3333")
                .address("555 Hospital Lane, Suite 10")
                .build();

        PatientProfile savedProfile = patientProfileRepository.save(profile);

        assertThat(savedProfile.getId()).isNotNull();
        assertThat(savedProfile.getCreatedAt()).isNotNull();
        assertThat(savedProfile.getUpdatedAt()).isNotNull();
        assertThat(savedProfile.getUser().getId()).isEqualTo(user.getId());

        Optional<PatientProfile> retrieved = patientProfileRepository.findByUserId(user.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getDateOfBirth()).isEqualTo(LocalDate.of(1993, 7, 12));
        assertThat(retrieved.get().getGender()).isEqualTo(Gender.FEMALE);
        assertThat(retrieved.get().getPhoneNumber()).isEqualTo("+1-555-444-3333");
        assertThat(retrieved.get().getAddress()).isEqualTo("555 Hospital Lane, Suite 10");
    }

    @Test
    @DisplayName("Enforce unique constraint on user_id in patient_profiles")
    void shouldEnforceUniqueConstraintOnUserId() {
        User user = userRepository.save(User.builder()
                .name("Duplicate Test Patient")
                .email("duplicate.patient@example.com")
                .password("Password123!")
                .role(Role.PATIENT)
                .status(Status.ACTIVE)
                .build());

        PatientProfile profile1 = PatientProfile.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .phoneNumber("555-111-2222")
                .address("1st Address")
                .build();
        patientProfileRepository.saveAndFlush(profile1);

        PatientProfile profile2 = PatientProfile.builder()
                .user(user)
                .dateOfBirth(LocalDate.of(1995, 2, 2))
                .gender(Gender.OTHER)
                .phoneNumber("555-333-4444")
                .address("2nd Address")
                .build();

        assertThatThrownBy(() -> patientProfileRepository.saveAndFlush(profile2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
