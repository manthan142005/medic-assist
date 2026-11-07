package com.mediassist.doctor;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for DoctorProfile entity operations.
 */
@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    @Query("SELECT d FROM DoctorProfile d JOIN FETCH d.user WHERE d.user.id = :userId")
    Optional<DoctorProfile> findByUserIdWithUser(@Param("userId") Long userId);

    @Query("SELECT d FROM DoctorProfile d JOIN FETCH d.user WHERE d.user.email = :email")
    Optional<DoctorProfile> findByUserEmailWithUser(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DoctorProfile d WHERE d.id = :id")
    Optional<DoctorProfile> findByIdForUpdate(@Param("id") Long id);

    boolean existsByUserId(Long userId);
}
