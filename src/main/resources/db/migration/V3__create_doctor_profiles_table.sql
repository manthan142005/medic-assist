-- Flyway Migration: V3__create_doctor_profiles_table.sql
-- Table: doctor_profiles

CREATE TABLE doctor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualifications VARCHAR(500) NOT NULL,
    years_of_experience INT NOT NULL,
    clinic_name VARCHAR(200) NOT NULL,
    clinic_address VARCHAR(255) NOT NULL,
    consultation_fee DECIMAL(10, 2) NULL,
    bio TEXT NULL,
    phone_number VARCHAR(20) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_doctor_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_doctor_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
