package com.mediassist.doctor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Entity representing an approved leave or holiday period for a doctor.
 */
@Entity
@Table(name = "doctor_leaves")
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "reason", nullable = true, length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DoctorLeave() {
    }

    public DoctorLeave(Long id, DoctorProfile doctor, LocalDate startDate, LocalDate endDate,
                       String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctor = doctor;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DoctorLeaveBuilder builder() {
        return new DoctorLeaveBuilder();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
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

    public DoctorProfile getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorProfile doctor) {
        this.doctor = doctor;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public static class DoctorLeaveBuilder {
        private Long id;
        private DoctorProfile doctor;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DoctorLeaveBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorLeaveBuilder doctor(DoctorProfile doctor) {
            this.doctor = doctor;
            return this;
        }

        public DoctorLeaveBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public DoctorLeaveBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public DoctorLeaveBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public DoctorLeaveBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorLeaveBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorLeave build() {
            return new DoctorLeave(id, doctor, startDate, endDate, reason, createdAt, updatedAt);
        }
    }
}

