package com.mediassist.doctor.dto;

import com.mediassist.doctor.DoctorProfile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for returning safe doctor profile information.
 * Excludes sensitive authentication data, secrets, or internal flags.
 */
public class DoctorProfileResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String displayName;
    private String specialization;
    private String qualifications;
    private Integer yearsOfExperience;
    private String clinicName;
    private String clinicAddress;
    private BigDecimal consultationFee;
    private String bio;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DoctorProfileResponse() {
    }

    public DoctorProfileResponse(Long id, Long userId, String name, String email,
                                 String displayName, String specialization, String qualifications,
                                 Integer yearsOfExperience, String clinicName, String clinicAddress,
                                 BigDecimal consultationFee, String bio, String phoneNumber,
                                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.displayName = displayName;
        this.specialization = specialization;
        this.qualifications = qualifications;
        this.yearsOfExperience = yearsOfExperience;
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.consultationFee = consultationFee;
        this.bio = bio;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DoctorProfileResponse fromEntity(DoctorProfile profile) {
        return DoctorProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .name(profile.getUser() != null ? profile.getUser().getName() : null)
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .displayName(profile.getDisplayName())
                .specialization(profile.getSpecialization())
                .qualifications(profile.getQualifications())
                .yearsOfExperience(profile.getYearsOfExperience())
                .clinicName(profile.getClinicName())
                .clinicAddress(profile.getClinicAddress())
                .consultationFee(profile.getConsultationFee())
                .bio(profile.getBio())
                .phoneNumber(profile.getPhoneNumber())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    public static DoctorProfileResponseBuilder builder() {
        return new DoctorProfileResponseBuilder();
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
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

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public static class DoctorProfileResponseBuilder {
        private Long id;
        private Long userId;
        private String name;
        private String email;
        private String displayName;
        private String specialization;
        private String qualifications;
        private Integer yearsOfExperience;
        private String clinicName;
        private String clinicAddress;
        private BigDecimal consultationFee;
        private String bio;
        private String phoneNumber;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public DoctorProfileResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorProfileResponseBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public DoctorProfileResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public DoctorProfileResponseBuilder email(String email) {
            this.email = email;
            return this;
        }

        public DoctorProfileResponseBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public DoctorProfileResponseBuilder specialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public DoctorProfileResponseBuilder qualifications(String qualifications) {
            this.qualifications = qualifications;
            return this;
        }

        public DoctorProfileResponseBuilder yearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
            return this;
        }

        public DoctorProfileResponseBuilder clinicName(String clinicName) {
            this.clinicName = clinicName;
            return this;
        }

        public DoctorProfileResponseBuilder clinicAddress(String clinicAddress) {
            this.clinicAddress = clinicAddress;
            return this;
        }

        public DoctorProfileResponseBuilder consultationFee(BigDecimal consultationFee) {
            this.consultationFee = consultationFee;
            return this;
        }

        public DoctorProfileResponseBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public DoctorProfileResponseBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public DoctorProfileResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorProfileResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorProfileResponse build() {
            return new DoctorProfileResponse(id, userId, name, email, displayName, specialization,
                    qualifications, yearsOfExperience, clinicName, clinicAddress, consultationFee,
                    bio, phoneNumber, createdAt, updatedAt);
        }
    }
}
