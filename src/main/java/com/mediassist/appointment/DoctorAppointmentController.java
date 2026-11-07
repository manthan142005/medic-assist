package com.mediassist.appointment;

import com.mediassist.appointment.dto.AppointmentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller exposing doctor-facing appointment operations:
 * reviewing assigned appointments, accepting, rejecting, and completing consultations.
 */
@RestController
@RequestMapping("/api/doctor/appointments")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public DoctorAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Retrieves all appointments assigned to the authenticated doctor.
     */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AppointmentResponse> responses = appointmentService.getDoctorAppointments(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    /**
     * Accepts a requested appointment assigned to the authenticated doctor.
     */
    @PatchMapping("/{appointmentId}/accept")
    public ResponseEntity<AppointmentResponse> acceptAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.acceptAppointment(userDetails.getUsername(), appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Rejects a requested appointment assigned to the authenticated doctor.
     */
    @PatchMapping("/{appointmentId}/reject")
    public ResponseEntity<AppointmentResponse> rejectAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.rejectAppointment(userDetails.getUsername(), appointmentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Marks an accepted appointment as completed by the authenticated doctor.
     */
    @PatchMapping("/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AppointmentResponse response = appointmentService.completeAppointment(userDetails.getUsername(), appointmentId);
        return ResponseEntity.ok(response);
    }
}

