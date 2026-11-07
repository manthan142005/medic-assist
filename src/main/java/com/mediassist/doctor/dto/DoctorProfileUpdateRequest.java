package com.mediassist.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request payload for updating an existing doctor profile.
 * Performs a controlled update of mutable profile attributes.
 * Immutable attributes (user, ownership, role, timestamps) are not accepted or modifiable.
 */
public class DoctorProfileUpdateRequest {

    @NotBlank(message = "Display name is required")
    @Size(min = 2, max = 150, message = "Display name must be between 2 and 150 characters")
    private String displayName;

    @NotBlank(message = "Specialization is required")
    @Size(min = 2, max = 100, message = "Specialization must be between 2 and 100 characters")
    private String specialization;

    @NotBlank(message = "Qualifications are required")
    @Size(min = 2, max = 500, message = "Qualifications must be between 2 and 500 characters")
    private String qualifications;

    @NotNull(message = "Years of experience is required")
    @Min(value = 0, message = "Years of experience must be 0 or greater")
    @Max(value = 70, message = "Years of experience must not exceed 70")
    private Integer yearsOfExperience;

    @NotBlank(message = "Clinic name is required")
    @Size(min = 2, max = 200, message = "Clinic name must be between 2 and 200 characters")
    private String clinicName;

    @NotBlank(message = "Clinic address is required")
    @Size(min = 5, max = 255, message = "Clinic address must be between 5 and 255 characters")
    private String clinicAddress;

    @DecimalMin(value = "0.00", inclusive = true, message = "Consultation fee must be 0.00 or greater")
    private BigDecimal consultationFee;

    @Size(max = 2000, message = "Bio must not exceed 2000 characters")
    private String bio;

    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Phone number must be a valid format between 7 and 20 digits")
    private String phoneNumber;

    public DoctorProfileUpdateRequest() {
    }

    public DoctorProfileUpdateRequest(String displayName, String specialization, String qualifications,
                                      Integer yearsOfExperience, String clinicName, String clinicAddress,
                                      BigDecimal consultationFee, String bio, String phoneNumber) {
        this.displayName = displayName;
        this.specialization = specialization;
        this.qualifications = qualifications;
        this.yearsOfExperience = yearsOfExperience;
        this.clinicName = clinicName;
        this.clinicAddress = clinicAddress;
        this.consultationFee = consultationFee;
        this.bio = bio;
        this.phoneNumber = phoneNumber;
    }

    public static DoctorProfileUpdateRequestBuilder builder() {
        return new DoctorProfileUpdateRequestBuilder();
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

    public static class DoctorProfileUpdateRequestBuilder {
        private String displayName;
        private String specialization;
        private String qualifications;
        private Integer yearsOfExperience;
        private String clinicName;
        private String clinicAddress;
        private BigDecimal consultationFee;
        private String bio;
        private String phoneNumber;

        public DoctorProfileUpdateRequestBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder specialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder qualifications(String qualifications) {
            this.qualifications = qualifications;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder yearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder clinicName(String clinicName) {
            this.clinicName = clinicName;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder clinicAddress(String clinicAddress) {
            this.clinicAddress = clinicAddress;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder consultationFee(BigDecimal consultationFee) {
            this.consultationFee = consultationFee;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public DoctorProfileUpdateRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public DoctorProfileUpdateRequest build() {
            return new DoctorProfileUpdateRequest(displayName, specialization, qualifications,
                    yearsOfExperience, clinicName, clinicAddress, consultationFee, bio, phoneNumber);
        }
    }
}
