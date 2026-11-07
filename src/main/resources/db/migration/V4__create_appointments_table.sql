-- Flyway Migration: V4__create_appointments_table.sql
-- Table: appointments

CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    reason VARCHAR(500) NOT NULL,
    notes TEXT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_appointments_patient FOREIGN KEY (patient_id) REFERENCES patient_profiles (id) ON DELETE CASCADE,
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctor_profiles (id) ON DELETE CASCADE
);

CREATE INDEX idx_appointments_patient_id ON appointments (patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments (doctor_id);
CREATE INDEX idx_appointments_date ON appointments (appointment_date);
CREATE INDEX idx_appointments_status ON appointments (status);
CREATE INDEX idx_appointments_doctor_date ON appointments (doctor_id, appointment_date);
CREATE INDEX idx_appointments_patient_date ON appointments (patient_id, appointment_date);

