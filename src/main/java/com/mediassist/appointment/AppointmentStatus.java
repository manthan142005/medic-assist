package com.mediassist.appointment;

/**
 * Enumeration representing the lifecycle states of an appointment.
 */
public enum AppointmentStatus {
    /**
     * Initial state when a patient books an appointment.
     */
    REQUESTED,

    /**
     * Confirmed by the assigned doctor.
     */
    ACCEPTED,

    /**
     * Declined by the assigned doctor.
     */
    REJECTED,

    /**
     * Cancelled by the patient prior to completion.
     */
    CANCELLED,

    /**
     * Successfully conducted and finalized by the doctor.
     */
    COMPLETED
}

