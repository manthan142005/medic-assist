-- Flyway Migration: V5__create_doctor_availability_table.sql
-- Table: doctor_availabilities

CREATE TABLE doctor_availabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week VARCHAR(15) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_doctor_availabilities_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_doctor_availabilities_doctor ON doctor_availabilities (doctor_id);
CREATE INDEX idx_doctor_availabilities_day ON doctor_availabilities (day_of_week);
CREATE INDEX idx_doctor_availabilities_doctor_day ON doctor_availabilities (doctor_id, day_of_week);

