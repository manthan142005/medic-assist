package com.mediassist.doctor;

import com.mediassist.auth.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a doctor's professional profile,
 * strictly linked one-to-one to a User account with the DOCTOR role.
 */
@Entity
@Table(name = "doctor_profiles")
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 150)
    private String displayName;

    @Column(name = "specialization", nullable = false, length = 100)
    private String specialization;

    @Column(name = "qualifications", nullable = false, length = 500)
    private String qualifications;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Column(name = "clinic_name", nullable = false, length = 200)
    private String clinicName;

    @Column(name = "clinic_address", nullable = false, length = 255)
    private String clinicAddress;

    @Column(name = "consultation_fee", nullable = true, precision = 10, scale = 2)
    private BigDecimal consultationFee;

    @Column(name = "bio", nullable = true, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DoctorProfile() {
    }

    public DoctorProfile(Long id, User user, String displayName, String specialization,
                         String qualifications, Integer yearsOfExperience, String clinicName,
                         String clinicAddress, BigDecimal consultationFee, String bio,
                         String phoneNumber, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
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

    public static DoctorProfileBuilder builder() {
        return new DoctorProfileBuilder();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public static class DoctorProfileBuilder {
        private Long id;
        private User user;
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

        public DoctorProfileBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DoctorProfileBuilder user(User user) {
            this.user = user;
            return this;
        }

        public DoctorProfileBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public DoctorProfileBuilder specialization(String specialization) {
            this.specialization = specialization;
            return this;
        }

        public DoctorProfileBuilder qualifications(String qualifications) {
            this.qualifications = qualifications;
            return this;
        }

        public DoctorProfileBuilder yearsOfExperience(Integer yearsOfExperience) {
            this.yearsOfExperience = yearsOfExperience;
            return this;
        }

        public DoctorProfileBuilder clinicName(String clinicName) {
            this.clinicName = clinicName;
            return this;
        }

        public DoctorProfileBuilder clinicAddress(String clinicAddress) {
            this.clinicAddress = clinicAddress;
            return this;
        }

        public DoctorProfileBuilder consultationFee(BigDecimal consultationFee) {
            this.consultationFee = consultationFee;
            return this;
        }

        public DoctorProfileBuilder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public DoctorProfileBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public DoctorProfileBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public DoctorProfileBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DoctorProfile build() {
            return new DoctorProfile(id, user, displayName, specialization, qualifications,
                    yearsOfExperience, clinicName, clinicAddress, consultationFee, bio,
                    phoneNumber, createdAt, updatedAt);
        }
    }
}
