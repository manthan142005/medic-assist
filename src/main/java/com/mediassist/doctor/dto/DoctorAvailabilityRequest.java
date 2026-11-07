package com.mediassist.doctor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Request payload for configuring a working session in a doctor's weekly schedule.
 */
public class DoctorAvailabilityRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime endTime;

    @Min(value = 5, message = "Slot duration must be at least 5 minutes")
    @Max(value = 240, message = "Slot duration cannot exceed 240 minutes")
    private Integer slotDurationMinutes = 30;

    private Boolean isActive = true;

    public DoctorAvailabilityRequest() {
    }

    public DoctorAvailabilityRequest(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                     Integer slotDurationMinutes, Boolean isActive) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes != null ? slotDurationMinutes : 30;
        this.isActive = isActive != null ? isActive : true;
    }

    public static DoctorAvailabilityRequestBuilder builder() {
        return new DoctorAvailabilityRequestBuilder();
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

    public static class DoctorAvailabilityRequestBuilder {
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMinutes = 30;
        private Boolean isActive = true;

        public DoctorAvailabilityRequestBuilder dayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
            return this;
        }

        public DoctorAvailabilityRequestBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public DoctorAvailabilityRequestBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public DoctorAvailabilityRequestBuilder slotDurationMinutes(Integer slotDurationMinutes) {
            this.slotDurationMinutes = slotDurationMinutes;
            return this;
        }

        public DoctorAvailabilityRequestBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public DoctorAvailabilityRequest build() {
            return new DoctorAvailabilityRequest(dayOfWeek, startTime, endTime, slotDurationMinutes, isActive);
        }
    }
}

