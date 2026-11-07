package com.mediassist.appointment;

import com.mediassist.appointment.dto.AppointmentBookingRequest;
import com.mediassist.appointment.dto.AppointmentResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing patient-facing appointment operations:
 * booking, listing personal appointments, viewing details, and cancellation.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Books a new appointment with the designated doctor.
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AppointmentBookingRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.bookAppointment(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all appointments booked by the authenticated patient.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AppointmentResponse> responses = appointmentService.getPatientAppointments(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves an appointment by ID.
     * Accessible by the patient who booked it, the doctor assigned to it, or an administrator.
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> getAppointmentById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.getAppointmentById(userDetails.getUsername(), appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an appointment booked by the authenticated patient.
     */
    @PatchMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.cancelAppointment(userDetails.getUsername(), appointmentId);
        return ResponseEntity.ok(response);
    }
}

