package com.mediassist.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserEntityMappingTest {

    private final UserRepository userRepository;

    @Autowired
    public UserEntityMappingTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    @DisplayName("User entity should be persisted and retrieved successfully from database")
    void shouldPersistAndRetrieveUser() {
        User user = User.builder()
                .name("Dr. John Doe")
                .email("john.doe@mediassist.com")
                .password("SecurePass123!")
                .role(Role.DOCTOR)
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        Optional<User> retrievedUser = userRepository.findByEmail("john.doe@mediassist.com");
        assertThat(retrievedUser).isPresent();
        assertThat(retrievedUser.get().getName()).isEqualTo("Dr. John Doe");
        assertThat(retrievedUser.get().getRole()).isEqualTo(Role.DOCTOR);
        assertThat(retrievedUser.get().getStatus()).isEqualTo(Status.ACTIVE);
    }
}
