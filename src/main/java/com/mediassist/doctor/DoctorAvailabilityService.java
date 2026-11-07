package com.mediassist.doctor;

import com.mediassist.appointment.Appointment;
import com.mediassist.appointment.AppointmentRepository;
import com.mediassist.appointment.AppointmentStatus;
import com.mediassist.auth.Role;
import com.mediassist.auth.Status;
import com.mediassist.auth.User;
import com.mediassist.auth.UserRepository;
import com.mediassist.doctor.dto.DoctorAvailabilityRequest;
import com.mediassist.doctor.dto.DoctorAvailabilityResponse;
import com.mediassist.doctor.dto.DoctorAvailableSlotsResponse;
import com.mediassist.doctor.dto.DoctorLeaveRequest;
import com.mediassist.doctor.dto.DoctorLeaveResponse;
import com.mediassist.doctor.dto.TimeSlotDto;
import com.mediassist.exception.AppointmentConflictException;
import com.mediassist.exception.AppointmentNotFoundException;
import com.mediassist.exception.DoctorScheduleNotFoundException;
import com.mediassist.exception.InvalidAppointmentException;
import com.mediassist.exception.ProfileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service managing doctor weekly working sessions, leave records,
 * and dynamic available appointment slot generation.
 */
@Service
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final DoctorLeaveRepository doctorLeaveRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public DoctorAvailabilityService(DoctorAvailabilityRepository doctorAvailabilityRepository,
                                   DoctorLeaveRepository doctorLeaveRepository,
                                   DoctorProfileRepository doctorProfileRepository,
                                   AppointmentRepository appointmentRepository,
                                   UserRepository userRepository) {
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.doctorLeaveRepository = doctorLeaveRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Adds a working availability session for the authenticated doctor.
     */
    @Transactional
    public DoctorAvailabilityResponse createAvailability(String doctorEmail, DoctorAvailabilityRequest request) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        validateSessionTime(request.getStartTime(), request.getEndTime());

        if (doctorAvailabilityRepository.existsOverlappingSession(
                doctor.getId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime(), null)) {
            throw new AppointmentConflictException("Working session overlaps with an existing session on " + request.getDayOfWeek());
        }

        DoctorAvailability availability = DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .slotDurationMinutes(request.getSlotDurationMinutes() != null ? request.getSlotDurationMinutes() : 30)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        DoctorAvailability saved = doctorAvailabilityRepository.save(availability);
        return DoctorAvailabilityResponse.fromEntity(saved);
    }

    /**
     * Retrieves all working availability sessions configured by the authenticated doctor.
     */
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getDoctorAvailability(String doctorEmail) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        return doctorAvailabilityRepository.findAllByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctor.getId())
                .stream()
                .map(DoctorAvailabilityResponse::fromEntity)
                .toList();
    }

    /**
     * Replaces or updates the entire weekly working schedule for the authenticated doctor.
     */
    @Transactional
    public List<DoctorAvailabilityResponse> updateDoctorSchedule(String doctorEmail, List<DoctorAvailabilityRequest> requests) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        if (requests == null || requests.isEmpty()) {
            doctorAvailabilityRepository.deleteAllByDoctorId(doctor.getId());
            return Collections.emptyList();
        }

        // Validate individual sessions and internal overlaps within the incoming batch
        for (int i = 0; i < requests.size(); i++) {
            DoctorAvailabilityRequest req1 = requests.get(i);
            validateSessionTime(req1.getStartTime(), req1.getEndTime());

            for (int j = i + 1; j < requests.size(); j++) {
                DoctorAvailabilityRequest req2 = requests.get(j);
                if (req1.getDayOfWeek() == req2.getDayOfWeek()) {
                    if (req1.getStartTime().isBefore(req2.getEndTime()) && req2.getStartTime().isBefore(req1.getEndTime())) {
                        throw new AppointmentConflictException("Schedule contains overlapping sessions on " + req1.getDayOfWeek());
                    }
                }
            }
        }

        // Clear existing and save new schedule
        doctorAvailabilityRepository.deleteAllByDoctorId(doctor.getId());

        List<DoctorAvailability> newSessions = requests.stream()
                .map(req -> DoctorAvailability.builder()
                        .doctor(doctor)
                        .dayOfWeek(req.getDayOfWeek())
                        .startTime(req.getStartTime())
                        .endTime(req.getEndTime())
                        .slotDurationMinutes(req.getSlotDurationMinutes() != null ? req.getSlotDurationMinutes() : 30)
                        .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                        .build())
                .toList();

        List<DoctorAvailability> savedSessions = doctorAvailabilityRepository.saveAll(newSessions);
        return savedSessions.stream().map(DoctorAvailabilityResponse::fromEntity).toList();
    }

    /**
     * Deletes a specific working availability session by ID.
     */
    @Transactional
    public void deleteAvailability(String doctorEmail, Long availabilityId) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);

        DoctorAvailability availability = doctorAvailabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new AppointmentNotFoundException("Availability session not found with id: " + availabilityId));

        if (!availability.getDoctor().getUser().getId().equals(doctorUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this availability session");
        }

        doctorAvailabilityRepository.delete(availability);
    }

    /**
     * Schedules an approved leave or holiday for the authenticated doctor.
     */
    @Transactional
    public DoctorLeaveResponse createLeave(String doctorEmail, DoctorLeaveRequest request) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate.isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException("Leave start date cannot be in the past");
        }

        if (endDate.isBefore(startDate)) {
            throw new InvalidAppointmentException("Leave end date cannot be before start date");
        }

        if (doctorLeaveRepository.existsOverlappingLeave(doctor.getId(), startDate, endDate, null)) {
            throw new AppointmentConflictException("Doctor already has leave scheduled overlapping with this period");
        }

        if (appointmentRepository.existsByDoctorIdAndStatusAndAppointmentDateBetween(
                doctor.getId(), AppointmentStatus.ACCEPTED, startDate, endDate)) {
            throw new AppointmentConflictException("Cannot schedule leave: doctor has accepted appointments between " +
                    startDate + " and " + endDate + ". Please resolve existing appointments before taking leave.");
        }

        DoctorLeave leave = DoctorLeave.builder()
                .doctor(doctor)
                .startDate(startDate)
                .endDate(endDate)
                .reason(request.getReason() != null ? request.getReason().trim() : null)
                .build();

        DoctorLeave saved = doctorLeaveRepository.save(leave);
        return DoctorLeaveResponse.fromEntity(saved);
    }

    /**
     * Retrieves all leave records for the authenticated doctor.
     */
    @Transactional(readOnly = true)
    public List<DoctorLeaveResponse> getDoctorLeaves(String doctorEmail) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);
        DoctorProfile doctor = doctorProfileRepository.findByUserIdWithUser(doctorUser.getId())
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for user: " + doctorUser.getEmail()));

        return doctorLeaveRepository.findAllByDoctorIdOrderByStartDateAsc(doctor.getId())
                .stream()
                .map(DoctorLeaveResponse::fromEntity)
                .toList();
    }

    /**
     * Cancels an upcoming leave record for the authenticated doctor.
     */
    @Transactional
    public void cancelLeave(String doctorEmail, Long leaveId) {
        User doctorUser = getAuthenticatedActiveDoctor(doctorEmail);

        DoctorLeave leave = doctorLeaveRepository.findById(leaveId)
                .orElseThrow(() -> new AppointmentNotFoundException("Leave record not found with id: " + leaveId));

        if (!leave.getDoctor().getUser().getId().equals(doctorUser.getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this leave record");
        }

        if (leave.getEndDate().isBefore(LocalDate.now())) {
            throw new InvalidAppointmentException("Cannot cancel past leave records");
        }

        doctorLeaveRepository.delete(leave);
    }

    /**
     * Computes dynamically available appointment slots for a doctor on a given date.
     * Excludes breaks, doctor leaves, past slots, and occupied appointments.
     */
    @Transactional(readOnly = true)
    public DoctorAvailableSlotsResponse getAvailableSlots(Long doctorId, LocalDate date) {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .or(() -> doctorProfileRepository.findByUserId(doctorId))
                .orElseThrow(() -> new ProfileNotFoundException("Doctor profile not found for doctor ID: " + doctorId));

        if (!doctorAvailabilityRepository.existsByDoctorId(doctor.getId())) {
            throw new DoctorScheduleNotFoundException("Doctor has not configured an availability schedule");
        }

        if (date.isBefore(LocalDate.now())) {
            return new DoctorAvailableSlotsResponse(doctor.getId(), date, Collections.emptyList());
        }

        if (doctorLeaveRepository.isDoctorOnLeave(doctor.getId(), date)) {
            return new DoctorAvailableSlotsResponse(doctor.getId(), date, Collections.emptyList());
        }

        List<DoctorAvailability> sessions = doctorAvailabilityRepository
                .findAllByDoctorIdAndDayOfWeekAndIsActiveTrueOrderByStartTimeAsc(doctor.getId(), date.getDayOfWeek());

        List<Appointment> occupiedAppointments = appointmentRepository
                .findAllByDoctorIdAndAppointmentDateAndStatusIn(
                        doctor.getId(), date,
                        List.of(AppointmentStatus.REQUESTED, AppointmentStatus.ACCEPTED, AppointmentStatus.COMPLETED)
                );

        List<TimeSlotDto> availableSlots = new ArrayList<>();
        LocalTime nowTime = LocalTime.now();
        boolean isToday = date.isEqual(LocalDate.now());

        for (DoctorAvailability session : sessions) {
            int duration = session.getSlotDurationMinutes() != null && session.getSlotDurationMinutes() > 0
                    ? session.getSlotDurationMinutes() : 30;

            LocalTime slotStart = session.getStartTime();
            while (true) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);
                if (slotEnd.isAfter(session.getEndTime())) {
                    break;
                }

                if (!isToday || slotStart.isAfter(nowTime)) {
                    final LocalTime fStart = slotStart;
                    final LocalTime fEnd = slotEnd;

                    boolean overlaps = occupiedAppointments.stream().anyMatch(a ->
                            a.getStartTime().isBefore(fEnd) && a.getEndTime().isAfter(fStart)
                    );

                    if (!overlaps) {
                        availableSlots.add(new TimeSlotDto(slotStart, slotEnd));
                    }
                }
                slotStart = slotEnd;
            }
        }

        return new DoctorAvailableSlotsResponse(doctor.getId(), date, availableSlots);
    }

    private void validateSessionTime(LocalTime startTime, LocalTime endTime) {
        if (!endTime.isAfter(startTime)) {
            throw new InvalidAppointmentException("End time must be after start time");
        }
    }

    private User getAuthenticatedActiveDoctor(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + normalizedEmail));

        if (user.getStatus() != Status.ACTIVE) {
            throw new AccessDeniedException("User account is inactive");
        }

        if (user.getRole() != Role.DOCTOR) {
            throw new AccessDeniedException("User does not have permission to manage doctor availability");
        }

        return user;
    }
}

