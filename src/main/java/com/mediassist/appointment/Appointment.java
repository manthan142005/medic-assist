package com.mediassist.appointment;

import com.mediassist.doctor.DoctorProfile;
import com.mediassist.patient.PatientProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a scheduled medical appointment between a patient and a doctor.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "notes", nullable = true, columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Appointment() {
    }

    public Appointment(Long id, PatientProfile patient, DoctorProfile doctor,
                       LocalDate appointmentDate, LocalTime startTime, LocalTime endTime,
                       String reason, String notes, AppointmentStatus status,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentBuilder builder() {
        return new AppointmentBuilder();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = AppointmentStatus.REQUESTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PatientProfile getPatient() {
        return patient;
    }

    public void setPatient(PatientProfile patient) {
        this.patient = patient;
    }

    public DoctorProfile getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorProfile doctor) {
        this.doctor = doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class AppointmentBuilder {
        private Long id;
        private PatientProfile patient;
        private DoctorProfile doctor;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
        private String notes;
        private AppointmentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AppointmentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AppointmentBuilder patient(PatientProfile patient) {
            this.patient = patient;
            return this;
        }

        public AppointmentBuilder doctor(DoctorProfile doctor) {
            this.doctor = doctor;
            return this;
        }

        public AppointmentBuilder appointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public AppointmentBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public AppointmentBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public AppointmentBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public AppointmentBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public AppointmentBuilder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public AppointmentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AppointmentBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Appointment build() {
            return new Appointment(id, patient, doctor, appointmentDate, startTime, endTime, reason, notes, status, createdAt, updatedAt);
        }
    }
}

