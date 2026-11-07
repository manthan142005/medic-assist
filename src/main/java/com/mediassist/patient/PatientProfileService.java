package com.mediassist.patient;

import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.exception.ProfileAlreadyExistsException;
import com.mediassist.exception.ProfileNotFoundException;
import com.mediassist.patient.dto.PatientProfileCreateRequest;
import com.mediassist.patient.dto.PatientProfileResponse;
import com.mediassist.patient.dto.PatientProfileUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing patient profile creation, retrieval, and updates.
 * Guarantees strict user ownership and prevents cross-user profile access.
 */
@Service
public class PatientProfileService {

    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public PatientProfileService(PatientProfileRepository patientProfileRepository,
                                 UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a profile for the currently authenticated patient user.
     * Rejects duplicate profile creation with ProfileAlreadyExistsException.
     *
     * @param userEmail email of the authenticated user
     * @param request profile creation parameters
     * @return safe response representation of the created profile
     */
    @Transactional
    public PatientProfileResponse createProfile(String userEmail, PatientProfileCreateRequest request) {
        User user = getAuthenticatedUser(userEmail);

        if (patientProfileRepository.existsByUserId(user.getId())) {
            throw new ProfileAlreadyExistsException("A patient profile already exists for user: " + user.getEmail());
        }

        PatientProfile profile = PatientProfile.builder()
                .user(user)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber().trim())
                .address(request.getAddress().trim())
                .build();

        PatientProfile savedProfile = patientProfileRepository.save(profile);
        return PatientProfileResponse.fromEntity(savedProfile);
    }

    /**
     * Retrieves the profile associated with the currently authenticated patient user.
     *
     * @param userEmail email of the authenticated user
     * @return safe profile response
     */
    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile(String userEmail) {
        User user = getAuthenticatedUser(userEmail);

        PatientProfile profile = patientProfileRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        return PatientProfileResponse.fromEntity(profile);
    }

    /**
     * Performs a controlled update of mutable profile attributes for the authenticated user.
     * User ownership, user ID, role, and email are strictly immutable.
     *
     * @param userEmail email of the authenticated user
     * @param request profile update parameters
     * @return updated safe profile response
     */
    @Transactional
    public PatientProfileResponse updateProfile(String userEmail, PatientProfileUpdateRequest request) {
        User user = getAuthenticatedUser(userEmail);

        PatientProfile profile = patientProfileRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Patient profile not found for user: " + user.getEmail()));

        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setPhoneNumber(request.getPhoneNumber().trim());
        profile.setAddress(request.getAddress().trim());

        PatientProfile updatedProfile = patientProfileRepository.save(profile);
        return PatientProfileResponse.fromEntity(updatedProfile);
    }

    /**
     * Resolves and verifies the authenticated User entity from email.
     */
    private User getAuthenticatedUser(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        if (user.getStatus() != Status.ACTIVE) {
            throw new AccessDeniedException("User account is inactive");
        }

        if (user.getRole() != Role.PATIENT) {
            throw new AccessDeniedException("User does not have permission to manage a patient profile");
        }

        return user;
    }
}
