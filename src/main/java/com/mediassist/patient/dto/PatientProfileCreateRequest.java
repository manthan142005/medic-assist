package com.mediassist.patient.dto;

import com.mediassist.patient.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request payload for creating a new patient profile.
 * Does NOT accept user ID or role to prevent identity spoofing.
 */
public class PatientProfileCreateRequest {

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9\\-\\s()]{7,20}$", message = "Phone number must be a valid format between 7 and 20 digits")
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    public PatientProfileCreateRequest() {
    }

    public PatientProfileCreateRequest(LocalDate dateOfBirth, Gender gender, String phoneNumber, String address) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public static PatientProfileCreateRequestBuilder builder() {
        return new PatientProfileCreateRequestBuilder();
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

    public static class PatientProfileCreateRequestBuilder {
        private LocalDate dateOfBirth;
        private Gender gender;
        private String phoneNumber;
        private String address;

        public PatientProfileCreateRequestBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public PatientProfileCreateRequestBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public PatientProfileCreateRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public PatientProfileCreateRequestBuilder address(String address) {
            this.address = address;
            return this;
        }

        public PatientProfileCreateRequest build() {
            return new PatientProfileCreateRequest(dateOfBirth, gender, phoneNumber, address);
        }
    }
}
