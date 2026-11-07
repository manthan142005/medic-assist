package com.mediassist.patient;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for PatientProfile entity operations.
 */
@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {

    Optional<PatientProfile> findByUserId(Long userId);

    @Query("SELECT p FROM PatientProfile p JOIN FETCH p.user WHERE p.user.id = :userId")
    Optional<PatientProfile> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT p FROM PatientProfile p JOIN FETCH p.user WHERE p.user.email = :email")
    Optional<PatientProfile> findByUserEmailWithUser(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PatientProfile p WHERE p.id = :id")
    Optional<PatientProfile> findByIdForUpdate(@Param("id") Long id);

    boolean existsByUserId(Long userId);
}
