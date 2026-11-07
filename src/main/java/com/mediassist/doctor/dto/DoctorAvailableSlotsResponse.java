package com.mediassist.doctor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Response payload for doctor available slots endpoint.
 */
public class DoctorAvailableSlotsResponse {

    private Long doctorId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private List<TimeSlotDto> availableSlots = new ArrayList<>();

    public DoctorAvailableSlotsResponse() {
    }

    public DoctorAvailableSlotsResponse(Long doctorId, LocalDate date, List<TimeSlotDto> availableSlots) {
        this.doctorId = doctorId;
        this.date = date;
        this.availableSlots = availableSlots != null ? availableSlots : new ArrayList<>();
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<TimeSlotDto> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlotDto> availableSlots) {
        this.availableSlots = availableSlots;
    }
}

