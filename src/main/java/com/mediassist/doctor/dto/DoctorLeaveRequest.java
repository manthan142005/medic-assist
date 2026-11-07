package com.mediassist.doctor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request payload for scheduling doctor leave.
 */
public class DoctorLeaveRequest {

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public DoctorLeaveRequest() {
    }

    public DoctorLeaveRequest(LocalDate startDate, LocalDate endDate, String reason) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
    }

    public static DoctorLeaveRequestBuilder builder() {
        return new DoctorLeaveRequestBuilder();
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

    public static class DoctorLeaveRequestBuilder {
        private LocalDate startDate;
        private LocalDate endDate;
        private String reason;

        public DoctorLeaveRequestBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public DoctorLeaveRequestBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public DoctorLeaveRequestBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public DoctorLeaveRequest build() {
            return new DoctorLeaveRequest(startDate, endDate, reason);
        }
    }
}

