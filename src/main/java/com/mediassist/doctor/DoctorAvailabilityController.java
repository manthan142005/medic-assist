package com.mediassist.doctor;

import com.mediassist.doctor.dto.DoctorAvailabilityRequest;
import com.mediassist.doctor.dto.DoctorAvailabilityResponse;
import com.mediassist.doctor.dto.DoctorAvailableSlotsResponse;
import com.mediassist.doctor.dto.DoctorLeaveRequest;
import com.mediassist.doctor.dto.DoctorLeaveResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * Controller exposing doctor schedule configuration, leave management,
 * and public/patient slot inquiry endpoints.
 */
@RestController
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService doctorAvailabilityService;

    @Autowired
    public DoctorAvailabilityController(DoctorAvailabilityService doctorAvailabilityService) {
        this.doctorAvailabilityService = doctorAvailabilityService;
    }

    // ─── Doctor Availability Endpoints ────────────────────────────────────────

    @PostMapping("/api/doctor/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorAvailabilityResponse> createAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DoctorAvailabilityResponse response = doctorAvailabilityService.createAvailability(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/doctor/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorAvailabilityResponse>> getDoctorAvailability(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<DoctorAvailabilityResponse> responses = doctorAvailabilityService.getDoctorAvailability(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/api/doctor/availability")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorAvailabilityResponse>> updateDoctorSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<@Valid DoctorAvailabilityRequest> requests) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<DoctorAvailabilityResponse> responses = doctorAvailabilityService.updateDoctorSchedule(userDetails.getUsername(), requests);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/api/doctor/availability/{availabilityId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> deleteAvailability(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long availabilityId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        doctorAvailabilityService.deleteAvailability(userDetails.getUsername(), availabilityId);
        return ResponseEntity.ok().build();
    }

    // ─── Doctor Leave Endpoints ───────────────────────────────────────────────

    @PostMapping("/api/doctor/leave")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorLeaveResponse> createLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorLeaveRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DoctorLeaveResponse response = doctorAvailabilityService.createLeave(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/doctor/leave")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorLeaveResponse>> getDoctorLeaves(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<DoctorLeaveResponse> responses = doctorAvailabilityService.getDoctorLeaves(userDetails.getUsername());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/api/doctor/leave/{leaveId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> cancelLeave(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long leaveId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        doctorAvailabilityService.cancelLeave(userDetails.getUsername(), leaveId);
        return ResponseEntity.ok().build();
    }

    // ─── Public / Patient Slot Inquiry Endpoint ───────────────────────────────

    @GetMapping("/api/doctors/{doctorId}/available-slots")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<DoctorAvailableSlotsResponse> getAvailableSlots(
            @PathVariable Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DoctorAvailableSlotsResponse response = doctorAvailabilityService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(response);
    }
}

