package com.mediassist.doctor;

import com.mediassist.doctor.dto.DoctorProfileCreateRequest;
import com.mediassist.doctor.dto.DoctorProfileResponse;
import com.mediassist.doctor.dto.DoctorProfileUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling doctor profile creation, retrieval, and updates.
 * Self-service endpoints require a valid JWT and the DOCTOR role.
 * Admin endpoint requires the ADMIN role.
 */
@RestController
public class DoctorProfileController {

    private final DoctorProfileService doctorProfileService;

    @Autowired
    public DoctorProfileController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    /**
     * Creates a profile for the currently authenticated doctor.
     * Identity is resolved strictly from the authenticated security context.
     *
     * @param userDetails authenticated user principal
     * @param request profile creation details
     * @return HTTP 201 Created with safe profile details
     */
    @PostMapping("/api/doctor/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorProfileCreateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DoctorProfileResponse response = doctorProfileService.createProfile(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves the profile of the currently authenticated doctor.
     *
     * @param userDetails authenticated user principal
     * @return HTTP 200 OK with safe profile details, or HTTP 404 if profile does not exist
     */
    @GetMapping("/api/doctor/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DoctorProfileResponse response = doctorProfileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates mutable profile details for the currently authenticated doctor.
     * Identity and ownership remain strictly immutable.
     *
     * @param userDetails authenticated user principal
     * @param request profile update details
     * @return HTTP 200 OK with updated profile details
     */
    @PutMapping("/api/doctor/profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DoctorProfileUpdateRequest request) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        DoctorProfileResponse response = doctorProfileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Allows an authenticated admin to view a doctor profile by its profile ID.
     *
     * @param doctorId the doctor profile ID
     * @return HTTP 200 OK with safe profile details, or HTTP 404 if not found
     */
    @GetMapping("/api/admin/doctors/{doctorId}/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorProfileResponse> getDoctorProfileForAdmin(
            @PathVariable Long doctorId) {
        DoctorProfileResponse response = doctorProfileService.getProfileByIdForAdmin(doctorId);
        return ResponseEntity.ok(response);
    }
}
