package com.mediassist.appointment;

import com.mediassist.appointment.dto.AppointmentBookingRequest;
import com.mediassist.appointment.dto.AppointmentResponse;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.DoctorAvailability;
import com.mediassist.doctor.DoctorAvailabilityRepository;
import com.mediassist.doctor.DoctorLeaveRepository;
import com.mediassist.doctor.DoctorProfile;
import com.mediassist.doctor.DoctorProfileRepository;
import com.mediassist.exception.AppointmentConflictException;
import com.mediassist.exception.AppointmentNotFoundException;
import com.mediassist.exception.InvalidAppointmentException;
import com.mediassist.exception.InvalidStatusTransitionException;
import com.mediassist.exception.ProfileNotFoundException;
import com.mediassist.patient.PatientProfile;
import com.mediassist.patient.PatientProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Service managing appointment scheduling, ownership authorization, state transitions,
 * and reliable conflict prevention.
 */
@Service
public class AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_SLOT_STATUSES = List.of(
            AppointmentStatus.REQUESTED,
            AppointmentStatus.ACCEPTED,
            AppointmentStatus.COMPLETED
    );

    private final AppointmentRepository appointmentRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final UserRepository userRepository;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientProfileRepository patientProfileRepository,
                              DoctorProfileRepository doctorProfileRepository,
                              DoctorAvailabilityRepository doctorAvailabilityRepository,
                              DoctorLeaveRepository doctorLeaveRepository,
                              UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.doctorLeaveRepository = doctorLeaveRepository;
        this.userRepository = userRepository;
    }

    /**
     * Books a new appointment for the authenticated patient with concurrency control and conflict checks.
     */
    @Transactional
    public AppointmentResponse bookAppointment(String patientEmail, AppointmentBookingRequest request) {
        User patientUser = getAuthenticatedActiveUser(patientEmail, Role.PATIENT);

        PatientProfile patient = patientProfileRepository.findByUserIdWithUser(patientUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Patient profile not found. A profile must be created before booking appointments."));

        DoctorProfile doctor = doctorProfileRepository.findById(request.getDoctorId())
                .or(() -> doctorProfileRepository.findByUserId(request.getDoctorId()))
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for doctor ID: " + request.getDoctorId()));

        if (doctor.getUser() == null || doctor.getUser().getStatus() != Status.ACTIVE || doctor.getUser().getRole() != Role.DOCTOR) {
            throw new ProfileNotFoundException("Doctor is not available or inactive for doctor ID: " + request.getDoctorId());
        }

        LocalDate appointmentDate = request.getAppointmentDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        // Validate appointment date
        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException("Appointment date cannot be in the past");
        }

        // Validate appointment time if scheduled for today
        if (appointmentDate.isEqual(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
            throw new InvalidAppointmentException("Appointment time cannot be in the past");
        }

        // Handle end time / duration
        if (endTime == null) {
            endTime = startTime.plusMinutes(30);
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }

        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        if (durationMinutes <= 0 || durationMinutes > 240) {
            throw new InvalidAppointmentException("Invalid appointment duration: " + durationMinutes + " minutes");
        }

        // Verify doctor is not on leave
        if (doctorLeaveRepository.isDoctorOnLeave(doctor.getId(), appointmentDate)) {
            throw new InvalidAppointmentException("Doctor is on leave on " + appointmentDate);
        }

        // Verify requested slot is within doctor's working schedule (if configured)
        if (doctorAvailabilityRepository.existsByDoctorId(doctor.getId())) {
            List<DoctorAvailability> sessions = doctorAvailabilityRepository
                    .findAllByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTimeAsc(doctor.getId(), appointmentDate.getDayOfWeek());

            final LocalTime fStart = startTime;
            final LocalTime fEnd = endTime;
            boolean insideWorkingHours = sessions.stream().anyMatch(s ->
                    !fStart.isBefore(s.getStartTime()) && !fEnd.isAfter(s.getEndTime())
            );

            if (!insideWorkingHours) {
                throw new InvalidAppointmentException("Requested appointment time is outside the doctor's working schedule");
            }
        }

        // Pessimistic lock to serialize concurrent bookings for the doctor and patient
        doctorProfileRepository.findByIdForUpdate(doctor.getId());
        patientProfileRepository.findByIdForUpdate(patient.getId());

        // Check for doctor conflict
        if (appointmentRepository.existsOverlappingAppointmentForDoctor(
                doctor.getId(), appointmentDate, startTime, endTime, ACTIVE_SLOT_STATUSES, null)) {
            throw new AppointmentConflictException("Doctor already has an appointment scheduled during this time slot");
        }

        // Check for patient conflict
        if (appointmentRepository.existsOverlappingAppointmentForPatient(
                patient.getId(), appointmentDate, startTime, endTime, ACTIVE_SLOT_STATUSES, null)) {
            throw new AppointmentConflictException("You already have an appointment scheduled during this time slot");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(appointmentDate)
                .startTime(startTime)
                .endTime(endTime)
                .reason(request.getReason().trim())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .status(AppointmentStatus.REQUESTED)
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    /**
     * Retrieves all appointments belonging to the authenticated patient.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(String patientEmail) {
        User patientUser = getAuthenticatedActiveUser(patientEmail, Role.PATIENT);
        PatientProfile patient = patientProfileRepository.findByUserIdWithUser(patientUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Patient profile not found for user: " + patientUser.getEmail()));

        return appointmentRepository.findAllByPatientIdWithDetails(patient.getId())
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves an appointment by ID with strict ownership authorization.
     * Patients may view only their own appointments; Doctors may view only their assigned appointments; Admins may view any.
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(String userEmail, Long appointmentId) {
        User user = userRepository.findByEmail(userEmail.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userEmail));

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        if (user.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to view this appointment");
            }
        } else if (user.getRole() == Role.DOCTOR) {
            if (!appointment.getDoctor().getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("You do not have permission to view this appointment");
            }
        } else if (user.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Access denied to view this appointment");
        }

        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Cancels an appointment requested by the authenticated patient.
     * Only REQUESTED or ACCEPTED appointments can be cancelled.
     */
    @Transactional
    public AppointmentResponse cancelAppointment(String patientEmail, Long appointmentId) {
        User patientUser = getAuthenticatedActiveUser(patientEmail, Role.PATIENT);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getPatient().getUser().getId().equals(patientUser.getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this appointment");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED ||
            appointment.getStatus() == AppointmentStatus.REJECTED ||
            appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Cannot cancel an appointment that is already " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    /**
     * Retrieves all appointments assigned to the authenticated doctor.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(String doctorEmail) {
        User doctorUser = getAuthenticatedActiveUser(doctorEmail, Role.DOCTOR);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        return appointmentRepository.findAllByDoctorIdWithDetails(doctor.getId())
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    /**
     * Accepts an appointment requested for the authenticated doctor.
     * Only appointments in REQUESTED status can be accepted, and no conflicting ACCEPTED appointment may exist.
     */
    @Transactional
    public AppointmentResponse acceptAppointment(String doctorEmail, Long appointmentId) {
        User doctorUser = getAuthenticatedActiveUser(doctorEmail, Role.DOCTOR);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new InvalidStatusTransitionException("Only appointments in REQUESTED status can be accepted. Current status: " + appointment.getStatus());
        }

        // Lock doctor row to serialize doctor confirmation checks
        doctorProfileRepository.findByIdForUpdate(appointment.getDoctor().getId());

        // Verify that no other conflicting appointment was already accepted in this slot
        if (appointmentRepository.existsOverlappingAppointmentForDoctor(
                appointment.getDoctor().getId(),
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                List.of(AppointmentStatus.ACCEPTED),
                appointment.getId())) {
            throw new AppointmentConflictException("Cannot accept appointment: conflicting accepted appointment exists at this time slot");
        }

        appointment.setStatus(AppointmentStatus.ACCEPTED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    /**
     * Rejects an appointment requested for the authenticated doctor.
     * Only appointments in REQUESTED status can be rejected.
     */
    @Transactional
    public AppointmentResponse rejectAppointment(String doctorEmail, Long appointmentId) {
        User doctorUser = getAuthenticatedActiveUser(doctorEmail, Role.DOCTOR);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            throw new InvalidStatusTransitionException("Only appointments in REQUESTED status can be rejected. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    /**
     * Marks an appointment as completed by the authenticated doctor.
     * Only appointments in ACCEPTED status can be completed.
     */
    @Transactional
    public AppointmentResponse completeAppointment(String doctorEmail, Long appointmentId) {
        User doctorUser = getAuthenticatedActiveUser(doctorEmail, Role.DOCTOR);

        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));

        if (!appointment.getDoctor().getUser().getId().equals(doctorUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.ACCEPTED) {
            throw new InvalidStatusTransitionException("Only appointments in ACCEPTED status can be marked as completed. Current status: " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        Appointment saved = appointmentRepository.save(appointment);
        return AppointmentResponse.fromEntity(saved);
    }

    /**
     * Retrieves all appointments across the system for administrator oversight.
     */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAllAppointmentsForAdmin() {
        return appointmentRepository.findAllWithDetails()
                .stream()
                .map(AppointmentResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieves any appointment by ID for administrator oversight.
     */
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentByIdForAdmin(Long appointmentId) {
        Appointment appointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + appointmentId));
        return AppointmentResponse.fromEntity(appointment);
    }

    /**
     * Helper to retrieve and validate an active user with the required role.
     */
    private User getAuthenticatedActiveUser(String email, Role requiredRole) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        if (user.getStatus() != Status.ACTIVE) {
            throw new AccessDeniedException("User account is inactive");
        }

        if (user.getRole() != requiredRole) {
            throw new AccessDeniedException("User does not have permission for this operation. Required role: " + requiredRole);
        }

        return user;
    }
}

