package com.mediassist.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Spring Data JPA repository for DoctorAvailability entity operations.
 */
@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    List<DoctorAvailability> findAllByDoctorIdOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

    List<DoctorAvailability> findAllByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTimeAsc(
            Long doctorId, DayOfWeek dayOfWeek);

    boolean existsByDoctorId(Long doctorId);

    boolean existsByDoctorIdAndIsActiveTrue(Long doctorId);

    void deleteAllByDoctorId(Long doctorId);

    @Query("SELECT COUNT(a) > 0 FROM DoctorAvailability a WHERE a.doctor.id = :doctorId " +
           "AND a.dayOfWeek = :dayOfWeek AND a.isActive = true " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsOverlappingSession(
            @Param("doctorId") Long doctorId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId);
}

