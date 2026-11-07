package com.mediassist.appointment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request payload for booking a new appointment.
 * Does NOT accept patient ownership or status to prevent spoofing.
 */
public class AppointmentBookingRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Appointment date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate appointmentDate;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime endTime;

    @NotBlank(message = "Appointment reason is required")
    @Size(min = 2, max = 500, message = "Appointment reason must be between 2 and 500 characters")
    private String reason;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    public AppointmentBookingRequest() {
    }

    public AppointmentBookingRequest(Long doctorId, LocalDate appointmentDate, LocalTime startTime,
                                     LocalTime endTime, String reason, String notes) {
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.notes = notes;
    }

    public static AppointmentBookingRequestBuilder builder() {
        return new AppointmentBookingRequestBuilder();
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
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

    public static class AppointmentBookingRequestBuilder {
        private Long doctorId;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
        private String notes;

        public AppointmentBookingRequestBuilder doctorId(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public AppointmentBookingRequestBuilder appointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public AppointmentBookingRequestBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public AppointmentBookingRequestBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public AppointmentBookingRequestBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public AppointmentBookingRequestBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public AppointmentBookingRequest build() {
            return new AppointmentBookingRequest(doctorId, appointmentDate, startTime, endTime, reason, notes);
        }
    }
}

