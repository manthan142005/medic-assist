package com.mediassist.appointment;

import com.mediassist.appointment.dto.AppointmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing administrative appointment endpoints for oversight and audits.
 */
@RestController
@RequestMapping("/api/admin/appointments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Retrieves all appointments across the system.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        List<AppointmentResponse> responses = appointmentService.getAllAppointmentsForAdmin();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves any appointment by ID.
     */
    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentResponse response = appointmentService.getAppointmentByIdForAdmin(appointmentId);
        return ResponseEntity.ok(response);
    }
}

