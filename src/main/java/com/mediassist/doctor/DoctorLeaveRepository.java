package com.mediassist.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for DoctorLeave entity operations.
 */
@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {

    List<DoctorLeave> findAllByDoctorIdOrderByStartDateAsc(Long doctorId);

    @Query("SELECT COUNT(l) > 0 FROM DoctorLeave l WHERE l.doctor.id = :doctorId " +
           "AND :date BETWEEN l.startDate AND l.endDate")
    boolean isDoctorOnLeave(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(l) > 0 FROM DoctorLeave l WHERE l.doctor.id = :doctorId " +
           "AND l.startDate <= :endDate AND l.endDate >= :startDate " +
           "AND (:excludeId IS NULL OR l.id != :excludeId)")
    boolean existsOverlappingLeave(
            @Param("doctorId") Long doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId);
}

