-- Flyway Migration: V2__create_patient_profiles_table.sql
-- Table: patient_profiles

CREATE TABLE patient_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_patient_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_patient_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);
