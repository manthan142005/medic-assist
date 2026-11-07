package com.mediassist.doctor.dto;

import com.mediassist.doctor.DoctorAvailability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for doctor availability response.
 */
public class DoctorAvailabilityResponse {

    private Long id;
    private Long doctorId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer slotDurationMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DoctorAvailabilityResponse() {
    }

    public DoctorAvailabilityResponse(Long id, Long doctorId, DayOfWeek dayOfWeek,
                                      LocalTime startTime, LocalTime endTime,
                                      Integer slotDurationMinutes, Boolean isActive,
                                      LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DoctorAvailabilityResponse fromEntity(DoctorAvailability availability) {
        return DoctorAvailabilityResponse.builder()
                .id(availability.getId())
                .doctorId(availability.getDoctor() != null ? availability.getDoctor().getId() : null)
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .slotDurationMinutes(availability.getSlotDurationMinutes())
                .isActive(availability.getIsActive())
                .createdAt(availability.getCreatedAt())
                .updatedAt(availability.getUpdatedAt())
                .build();
    }

    public static DoctorAvailabilityResponseBuilder builder() {
        return new DoctorAvailabilityResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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

    public static class DoctorAvailabilityResponseBuilder {
        private Long id;
        private Long doctorId;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMinutes;
        private Boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DoctorAvailabilityResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorAvailabilityResponseBuilder doctorId(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public DoctorAvailabilityResponseBuilder dayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        public DoctorAvailabilityResponseBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public DoctorAvailabilityResponseBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public DoctorAvailabilityResponseBuilder slotDurationMinutes(Integer slotDurationMinutes) {
            this.slotDurationMinutes = slotDurationMinutes;
            return this;
        }

        public DoctorAvailabilityResponseBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public DoctorAvailabilityResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorAvailabilityResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorAvailabilityResponse build() {
            return new DoctorAvailabilityResponse(id, doctorId, dayOfWeek, startTime, endTime,
                    slotDurationMinutes, isActive, createdAt, updatedAt);
        }
    }
}

