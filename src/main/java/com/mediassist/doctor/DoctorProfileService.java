package com.mediassist.doctor;

import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.dto.DoctorProfileCreateRequest;
import com.mediassist.doctor.dto.DoctorProfileResponse;
import com.mediassist.doctor.dto.DoctorProfileUpdateRequest;
import com.mediassist.exception.ProfileAlreadyExistsException;
import com.mediassist.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing doctor profile creation, retrieval, and updates.
 * Guarantees strict user ownership and prevents cross-user profile access.
 */
@Service
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorProfileService(DoctorProfileRepository doctorProfileRepository,
                                UserRepository userRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a profile for the currently authenticated doctor user.
     * Rejects duplicate profile creation with ProfileAlreadyExistsException.
     *
     * @param userEmail email of the authenticated user
     * @param request profile creation parameters
     * @return safe response representation of the created profile
     */
    @Transactional
    public DoctorProfileResponse createProfile(String userEmail, DoctorProfileCreateRequest request) {
        User user = getAuthenticatedDoctor(userEmail);

        if (doctorProfileRepository.existsByUserId(user.getId())) {
            throw new ProfileAlreadyExistsException("A doctor profile already exists for user: " + user.getEmail());
        }

        DoctorProfile profile = DoctorProfile.builder()
                .user(user)
                .displayName(request.getDisplayName().trim())
                .specialization(request.getSpecialization().trim())
                .qualifications(request.getQualifications().trim())
                .yearsOfExperience(request.getYearsOfExperience())
                .clinicName(request.getClinicName().trim())
                .clinicAddress(request.getClinicAddress().trim())
                .consultationFee(request.getConsultationFee())
                .bio(request.getBio() != null ? request.getBio().trim() : null)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .build();

        DoctorProfile savedProfile = doctorProfileRepository.save(profile);
        return DoctorProfileResponse.fromEntity(savedProfile);
    }

    /**
     * Retrieves the profile associated with the currently authenticated doctor user.
     *
     * @param userEmail email of the authenticated user
     * @return safe profile response
     */
    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfile(String userEmail) {
        User user = getAuthenticatedDoctor(userEmail);

        DoctorProfile profile = doctorProfileRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + user.getEmail()));

        return DoctorProfileResponse.fromEntity(profile);
    }

    /**
     * Performs a controlled update of mutable profile attributes for the authenticated doctor user.
     * User ownership, user ID, role, and email are strictly immutable.
     *
     * @param userEmail email of the authenticated user
     * @param request profile update parameters
     * @return updated safe profile response
     */
    @Transactional
    public DoctorProfileResponse updateProfile(String userEmail, DoctorProfileUpdateRequest request) {
        User user = getAuthenticatedDoctor(userEmail);

        DoctorProfile profile = doctorProfileRepository.findByUserIdWithUser(user.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + user.getEmail()));

        profile.setDisplayName(request.getDisplayName().trim());
        profile.setSpecialization(request.getSpecialization().trim());
        profile.setQualifications(request.getQualifications().trim());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setClinicName(request.getClinicName().trim());
        profile.setClinicAddress(request.getClinicAddress().trim());
        profile.setConsultationFee(request.getConsultationFee());
        profile.setBio(request.getBio() != null ? request.getBio().trim() : null);
        profile.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);

        DoctorProfile updatedProfile = doctorProfileRepository.save(profile);
        return DoctorProfileResponse.fromEntity(updatedProfile);
    }

    /**
     * Retrieves a doctor profile by its profile ID for admin use.
     * Does not enforce ownership — admin access only, enforced by controller-level @PreAuthorize.
     *
     * @param doctorProfileId the doctor profile ID
     * @return safe profile response
     */
    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfileByIdForAdmin(Long doctorProfileId) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found with id: " + doctorProfileId));

        return DoctorProfileResponse.fromEntity(profile);
    }

    /**
     * Resolves and verifies the authenticated User entity from email,
     * asserting that the user holds the DOCTOR role and is ACTIVE.
     */
    private User getAuthenticatedDoctor(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        if (user.getStatus() != Status.ACTIVE) {
            throw new AccessDeniedException("User account is inactive");
        }

        if (user.getRole() != Role.DOCTOR) {
            throw new AccessDeniedException("User does not have permission to manage a doctor profile");
        }

        return user;
    }
}
