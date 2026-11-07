package com.mediassist.patient.dto;

import com.mediassist.patient.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Request payload for updating an existing patient profile.
 * Performs a controlled update of mutable profile attributes.
 * Immutable attributes (user, ownership, role, timestamps) are not accepted or modifiable.
 */
public class PatientProfileUpdateRequest {

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

    public PatientProfileUpdateRequest() {
    }

    public PatientProfileUpdateRequest(LocalDate dateOfBirth, Gender gender, String phoneNumber, String address) {
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public static PatientProfileUpdateRequestBuilder builder() {
        return new PatientProfileUpdateRequestBuilder();
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

    public static class PatientProfileUpdateRequestBuilder {
        private LocalDate dateOfBirth;
        private Gender gender;
        private String phoneNumber;
        private String address;

        public PatientProfileUpdateRequestBuilder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public PatientProfileUpdateRequestBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public PatientProfileUpdateRequestBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public PatientProfileUpdateRequestBuilder address(String address) {
            this.address = address;
            return this;
        }

        public PatientProfileUpdateRequest build() {
            return new PatientProfileUpdateRequest(dateOfBirth, gender, phoneNumber, address);
        }
    }
}
