package com.mediassist.appointment.dto;

import com.mediassist.appointment.Appointment;
import com.mediassist.appointment.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object for appointment responses.
 * Never leaks passwords or internal security hashes.
 */
public class AppointmentResponse {

    private Long id;
    private Long patientId;
    private Long patientUserId;
    private String patientName;
    private String patientEmail;
    private String patientPhoneNumber;
    private Long doctorId;
    private Long doctorUserId;
    private String doctorName;
    private String doctorDisplayName;
    private String doctorSpecialization;
    private String clinicName;
    private String clinicAddress;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private String notes;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AppointmentResponse() {
    }

    public AppointmentResponse(Long id, Long patientId, Long patientUserId, String patientName,
                               String patientEmail, String patientPhoneNumber, Long doctorId,
                               Long doctorUserId, String doctorName, String doctorDisplayName,
                               String doctorSpecialization, String clinicName, String clinicAddress,
                               LocalDate appointmentDate, LocalTime startTime, LocalTime endTime,
                               String reason, String notes, AppointmentStatus status,
                               LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientUserId = patientUserId;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.patientPhoneNumber = patientPhoneNumber;
        this.doctorId = doctorId;
        this.doctorUserId = doctorUserId;
        this.doctorName = doctorName;
        this.doctorDisplayName = doctorDisplayName;
        this.doctorSpecialization = doctorSpecialization;
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.notes = notes;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AppointmentResponse fromEntity(Appointment appointment) {
        Long pId = null;
        Long pUserId = null;
        String pName = null;
        String pEmail = null;
        String pPhone = null;
        if (appointment.getPatient() != null) {
            pId = appointment.getPatient().getId();
            pPhone = appointment.getPatient().getPhoneNumber();
            if (appointment.getPatient().getUser() != null) {
                pUserId = appointment.getPatient().getUser().getId();
                pName = appointment.getPatient().getUser().getName();
                pEmail = appointment.getPatient().getUser().getEmail();
            }
        }

        Long dId = null;
        Long dUserId = null;
        String dName = null;
        String dDisplayName = null;
        String dSpec = null;
        String cName = null;
        String cAddress = null;
        if (appointment.getDoctor() != null) {
            dId = appointment.getDoctor().getId();
            dDisplayName = appointment.getDoctor().getDisplayName();
            dSpec = appointment.getDoctor().getSpecialization();
            cName = appointment.getDoctor().getClinicName();
            cAddress = appointment.getDoctor().getClinicAddress();
            if (appointment.getDoctor().getUser() != null) {
                dUserId = appointment.getDoctor().getUser().getId();
                dName = appointment.getDoctor().getUser().getName();
            }
        }

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(pId)
                .patientUserId(pUserId)
                .patientName(pName)
                .patientEmail(pEmail)
                .patientPhoneNumber(pPhone)
                .doctorId(dId)
                .doctorUserId(dUserId)
                .doctorName(dName)
                .doctorDisplayName(dDisplayName)
                .doctorSpecialization(dSpec)
                .clinicName(cName)
                .clinicAddress(cAddress)
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .reason(appointment.getReason())
                .notes(appointment.getNotes())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    public static AppointmentResponseBuilder builder() {
        return new AppointmentResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getPatientPhoneNumber() {
        return patientPhoneNumber;
    }

    public void setPatientPhoneNumber(String patientPhoneNumber) {
        this.patientPhoneNumber = patientPhoneNumber;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getDoctorUserId() {
        return doctorUserId;
    }

    public void setDoctorUserId(Long doctorUserId) {
        this.doctorUserId = doctorUserId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorDisplayName() {
        return doctorDisplayName;
    }

    public void setDoctorDisplayName(String doctorDisplayName) {
        this.doctorDisplayName = doctorDisplayName;
    }

    public String getDoctorSpecialization() {
        return doctorSpecialization;
    }

    public void setDoctorSpecialization(String doctorSpecialization) {
        this.doctorSpecialization = doctorSpecialization;
    }

    public String getClinicName() {
        return clinicName;
    }

    public void setClinicName(String clinicName) {
        this.clinicName = clinicName;
    }

    public String getClinicAddress() {
        return clinicAddress;
    }

    public void setClinicAddress(String clinicAddress) {
        this.clinicAddress = clinicAddress;
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

    public static class AppointmentResponseBuilder {
        private Long id;
        private Long patientId;
        private Long patientUserId;
        private String patientName;
        private String patientEmail;
        private String patientPhoneNumber;
        private Long doctorId;
        private Long doctorUserId;
        private String doctorName;
        private String doctorDisplayName;
        private String doctorSpecialization;
        private String clinicName;
        private String clinicAddress;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private String reason;
        private String notes;
        private AppointmentStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public AppointmentResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AppointmentResponseBuilder patientId(Long patientId) {
            this.patientId = patientId;
            return this;
        }

        public AppointmentResponseBuilder patientUserId(Long patientUserId) {
            this.patientUserId = patientUserId;
            return this;
        }

        public AppointmentResponseBuilder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public AppointmentResponseBuilder patientEmail(String patientEmail) {
            this.patientEmail = patientEmail;
            return this;
        }

        public AppointmentResponseBuilder patientPhoneNumber(String patientPhoneNumber) {
            this.patientPhoneNumber = patientPhoneNumber;
            return this;
        }

        public AppointmentResponseBuilder doctorId(Long doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public AppointmentResponseBuilder doctorUserId(Long doctorUserId) {
            this.doctorUserId = doctorUserId;
            return this;
        }

        public AppointmentResponseBuilder doctorName(String doctorName) {
            this.doctorName = doctorName;
            return this;
        }

        public AppointmentResponseBuilder doctorDisplayName(String doctorDisplayName) {
            this.doctorDisplayName = doctorDisplayName;
            return this;
        }

        public AppointmentResponseBuilder doctorSpecialization(String doctorSpecialization) {
            this.doctorSpecialization = doctorSpecialization;
            return this;
        }

        public AppointmentResponseBuilder clinicName(String clinicName) {
            this.clinicName = clinicName;
            return this;
        }

        public AppointmentResponseBuilder clinicAddress(String clinicAddress) {
            this.clinicAddress = clinicAddress;
            return this;
        }

        public AppointmentResponseBuilder appointmentDate(LocalDate appointmentDate) {
            this.appointmentDate = appointmentDate;
            return this;
        }

        public AppointmentResponseBuilder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public AppointmentResponseBuilder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public AppointmentResponseBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public AppointmentResponseBuilder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public AppointmentResponseBuilder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public AppointmentResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AppointmentResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AppointmentResponse build() {
            return new AppointmentResponse(id, patientId, patientUserId, patientName, patientEmail, patientPhoneNumber,
                    doctorId, doctorUserId, doctorName, doctorDisplayName, doctorSpecialization, clinicName,
                    clinicAddress, appointmentDate, startTime, endTime, reason, notes, status, createdAt, updatedAt);
        }
    }
}

