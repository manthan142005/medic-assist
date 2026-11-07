package com.mediassist.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Appointment entity operations.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user " +
           "WHERE a.id = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user " +
           "WHERE a.patient.id = :patientId " +
           "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findAllByPatientIdWithDetails(@Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user " +
           "WHERE a.doctor.id = :doctorId " +
           "ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findAllByDoctorIdWithDetails(@Param("doctorId") Long doctorId);

    @Query("SELECT a FROM Appointment a " +
           "JOIN FETCH a.patient p JOIN FETCH p.user " +
           "JOIN FETCH a.doctor d JOIN FETCH d.user " +
           "ORDER BY a.appointmentDate DESC, a.startTime DESC")
    List<Appointment> findAllWithDetails();

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.status IN :statuses " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsOverlappingAppointmentForDoctor(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.status IN :statuses " +
           "AND a.startTime < :endTime AND a.endTime > :startTime " +
           "AND (:excludeId IS NULL OR a.id != :excludeId)")
    boolean existsOverlappingAppointmentForPatient(
            @Param("patientId") Long patientId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("excludeId") Long excludeId
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.status = :status " +
           "AND a.appointmentDate BETWEEN :startDate AND :endDate")
    boolean existsByDoctorIdAndStatusAndAppointmentDateBetween(
            @Param("doctorId") Long doctorId,
            @Param("status") AppointmentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :appointmentDate " +
           "AND a.status IN :statuses")
    List<Appointment> findAllByDoctorIdAndAppointmentDateAndStatusIn(
            @Param("doctorId") Long doctorId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("statuses") Collection<AppointmentStatus> statuses
    );
}

