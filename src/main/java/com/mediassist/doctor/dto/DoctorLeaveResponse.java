package com.mediassist.doctor.dto;

import com.mediassist.doctor.DoctorLeave;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for doctor leave response.
 */
public class DoctorLeaveResponse {

    private Long id;
    private Long doctorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DoctorLeaveResponse() {
    }

    public DoctorLeaveResponse(Long id, Long doctorId, LocalDate startDate, LocalDate endDate,
                               String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DoctorLeaveResponse fromEntity(DoctorLeave leave) {
        return DoctorLeaveResponse.builder()
                .id(leave.getId())
                .doctorId(leave.getDoctor() != null ? leave.getDoctor().getId() : null)
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .reason(leave.getReason())
                .createdAt(leave.getCreatedAt())
                .updatedAt(leave.getUpdatedAt())
                .build();
    }

    public static DoctorLeaveResponseBuilder builder() {
        return new DoctorLeaveResponseBuilder();
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

    public static class DoctorLeaveResponseBuilder {
        private Long id;
        private Long doctorId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DoctorLeaveResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorLeaveResponseBuilder doctorId(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public DoctorLeaveResponseBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public DoctorLeaveResponseBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public DoctorLeaveResponseBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public DoctorLeaveResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorLeaveResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorLeaveResponse build() {
            return new DoctorLeaveResponse(id, doctorId, startDate, endDate, reason, createdAt, updatedAt);
        }
    }
}

