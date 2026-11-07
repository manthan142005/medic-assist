package com.mediassist.doctor;

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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a recurring weekly working availability session for a doctor.
 */
@Entity
@Table(name = "doctor_availabilities")
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorProfile doctor;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 15)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "slot_duration_minutes", nullable = false)
    private Integer slotDurationMinutes;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DoctorAvailability() {
    }

    public DoctorAvailability(Long id, DoctorProfile doctor, DayOfWeek dayOfWeek,
                              LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes,
                              Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctor = doctor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 30;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DoctorAvailabilityBuilder builder() {
        return new DoctorAvailabilityBuilder();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.slotDurationMinutes == null) {
            this.slotDurationMinutes = 30;
        }
        if (this.isActive == null) {
            this.isActive = true;
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

    public DoctorProfile getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorProfile doctor) {
        this.doctor = doctor;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
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

    public Integer getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public void setSlotDurationMinutes(Integer slotDurationMinutes) {
        this.slotDurationMinutes = slotDurationMinutes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
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

    public static class DoctorAvailabilityBuilder {
        private Long id;
        private DoctorProfile doctor;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMinutes = 30;
        private Boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DoctorAvailabilityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorAvailabilityBuilder doctor(DoctorProfile doctor) {
            this.doctor = doctor;
            return this;
        }

        public DoctorAvailabilityBuilder dayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        public DoctorAvailabilityBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public DoctorAvailabilityBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public DoctorAvailabilityBuilder slotDurationMinutes(Integer slotDurationMinutes) {
            this.slotDurationMinutes = slotDurationMinutes;
            return this;
        }

        public DoctorAvailabilityBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public DoctorAvailabilityBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorAvailabilityBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorAvailability build() {
            return new DoctorAvailability(id, doctor, dayOfWeek, startTime, endTime,
                    slotDurationMinutes, isActive, createdAt, updatedAt);
        }
    }
}

