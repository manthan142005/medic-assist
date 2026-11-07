package com.mediassist.patient;

import com.mediassist.patient.dto.PatientProfileCreateRequest;
import com.mediassist.patient.dto.PatientProfileResponse;
import com.mediassist.patient.dto.PatientProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling patient profile creation, retrieval, and updates.
 * All endpoints require a valid JWT and the PATIENT role.
 */
@RestController
@RequestMapping("/api/patient/profile")
@PreAuthorize("hasRole('PATIENT')")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    @Autowired
    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    /**
     * Creates a profile for the currently authenticated patient.
     * Identity is resolved strictly from the authenticated security context.
     *
     * @param userDetails authenticated user principal
     * @param request profile creation details
     * @return HTTP 201 Created with safe profile details
     */
    @PostMapping
    public ResponseEntity<PatientProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientProfileCreateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PatientProfileResponse response = patientProfileService.createProfile(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the profile of the currently authenticated patient.
     *
     * @param userDetails authenticated user principal
     * @return HTTP 200 OK with safe profile details, or HTTP 404 if profile does not exist
     */
    @GetMapping
    public ResponseEntity<PatientProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PatientProfileResponse response = patientProfileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates mutable profile details for the currently authenticated patient.
     * Identity and ownership remain strictly immutable.
     *
     * @param userDetails authenticated user principal
     * @param request profile update details
     * @return HTTP 200 OK with updated profile details
     */
    @PutMapping
    public ResponseEntity<PatientProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientProfileUpdateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PatientProfileResponse response = patientProfileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }
}
