package com.mediassist.patient.dto;

import com.mediassist.patient.Gender;
import com.mediassist.patient.PatientProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for returning safe patient profile information.
 * Excludes sensitive authentication data, secrets, or internal flags.
 */
public class PatientProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phoneNumber;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PatientProfileResponse() {
    }

    public PatientProfileResponse(Long id, Long userId, String name, String email,
                                  LocalDate dateOfBirth, Gender gender, String phoneNumber,
                                  String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PatientProfileResponse fromEntity(PatientProfile profile) {
        return PatientProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .name(profile.getUser() != null ? profile.getUser().getName() : null)
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public static PatientProfileResponseBuilder builder() {
        return new PatientProfileResponseBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public static class PatientProfileResponseBuilder {
        private Long id;
        private Long userId;
        private String name;
        private String email;
        private LocalDate dateOfBirth;
        private Gender gender;
        private String phoneNumber;
        private String address;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PatientProfileResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PatientProfileResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public PatientProfileResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PatientProfileResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public PatientProfileResponseBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public PatientProfileResponseBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public PatientProfileResponseBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public PatientProfileResponseBuilder address(String address) {
            this.address = address;
            return this;
        }

        public PatientProfileResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PatientProfileResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PatientProfileResponse build() {
            return new PatientProfileResponse(id, userId, name, email, dateOfBirth, gender, phoneNumber, address, createdAt, updatedAt);
        }
    }
}
