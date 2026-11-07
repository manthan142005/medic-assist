-- Flyway Migration: V6__create_doctor_leave_table.sql
-- Table: doctor_leaves

CREATE TABLE doctor_leaves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_doctor_leaves_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_doctor_leaves_doctor ON doctor_leaves (doctor_id);
CREATE INDEX idx_doctor_leaves_dates ON doctor_leaves (start_date, end_date);
CREATE INDEX idx_doctor_leaves_doctor_dates ON doctor_leaves (doctor_id, start_date, end_date);

