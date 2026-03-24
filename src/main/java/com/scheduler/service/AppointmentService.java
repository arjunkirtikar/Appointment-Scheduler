package com.scheduler.service;

import com.scheduler.domain.Appointment;
import com.scheduler.domain.StudentGroup;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.StudentGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final StudentGroupRepository studentGroupRepository;

    @Value("${semester.start}")
    private String semesterStart;

    @Value("${semester.end}")
    private String semesterEnd;

    public AppointmentService(AppointmentRepository appointmentRepository,
            StudentGroupRepository studentGroupRepository) {
        this.appointmentRepository = appointmentRepository;
        this.studentGroupRepository = studentGroupRepository;
    }

    // ── Read Methods (readOnly for performance) ───────────────────

    @Transactional(readOnly = true)
    public List<Appointment> getAvailableSlots() {
        return appointmentRepository.findByIsBookedFalse();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getStudentBookings(String username) {
        return appointmentRepository.findByStudentUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getInstructorSlots(String username) {
        return appointmentRepository.findByInstructorUsername(username);
    }

    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    @Transactional(readOnly = true)
    public List<StudentGroup> getAllGroups() {
        return studentGroupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public StudentGroup getGroupById(Long id) {
        return studentGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Transactional(readOnly = true)
    public boolean isStudentInAnyGroup(String username) {
        List<StudentGroup> groups = studentGroupRepository
                .findByMemberUsernamesContaining(username);
        return !groups.isEmpty();
    }

    // ── Write Methods ─────────────────────────────────────────────

    public void bookAppointment(Long id, String studentUsername) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        if (appt.isBooked())
            throw new RuntimeException("Already booked");
        appt.setBooked(true);
        appt.setStudentUsername(studentUsername);
        appointmentRepository.save(appt);
    }

    public void createBulkSlots(String instructorUsername,
            LocalDateTime startTime,
            int durationMinutes,
            int breakMinutes,
            int numberOfSlots,
            boolean isGroup,
            int groupCapacity,
            int slotsBeforeLongBreak,
            int longBreakMinutes) {

        // Semester constraint check
        LocalDate semStart = LocalDate.parse(semesterStart);
        LocalDate semEnd = LocalDate.parse(semesterEnd);
        LocalDate slotDate = startTime.toLocalDate();
        if (slotDate.isBefore(semStart) || slotDate.isAfter(semEnd)) {
            throw new RuntimeException("Slots must be within the semester: "
                    + semesterStart + " to " + semesterEnd);
        }

        LocalDateTime current = startTime;
        for (int i = 0; i < numberOfSlots; i++) {
            Appointment appt = new Appointment();
            appt.setInstructorUsername(instructorUsername);
            appt.setSlotTime(current);
            appt.setDurationMinutes(durationMinutes);
            appt.setBooked(false);
            appt.setGroupAppointment(isGroup);
            appt.setGroupCapacity(isGroup ? groupCapacity : 1);
            appt.setCancellationDeadlineHours(24); // default 24hr notice
            appointmentRepository.save(appt);

            // Long break after every N slots, otherwise regular break
            if (slotsBeforeLongBreak > 0 && (i + 1) % slotsBeforeLongBreak == 0) {
                current = current.plusMinutes(durationMinutes + longBreakMinutes);
            } else {
                current = current.plusMinutes(durationMinutes + breakMinutes);
            }
        }
    }

    public void cancelByStudent(Long id, String studentUsername) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!studentUsername.equals(appt.getStudentUsername())) {
            throw new RuntimeException("You can only cancel your own appointments");
        }

        long hoursUntilSlot = java.time.Duration.between(
                LocalDateTime.now(), appt.getSlotTime()).toHours();

        if (hoursUntilSlot < appt.getCancellationDeadlineHours()) {
            throw new RuntimeException("Cannot cancel — minimum "
                    + appt.getCancellationDeadlineHours()
                    + " hours notice required. Slot is in "
                    + hoursUntilSlot + " hours.");
        }

        appt.setBooked(false);
        appt.setStudentUsername(null);
        appointmentRepository.save(appt);
    }

    public void cancelByInstructor(Long id, String instructorUsername) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!instructorUsername.equals(appt.getInstructorUsername())) {
            throw new RuntimeException("You can only cancel your own slots");
        }

        appointmentRepository.delete(appt);
    }

    public StudentGroup createGroup(String groupName, String createdByUsername) {
        StudentGroup group = new StudentGroup();
        group.setGroupName(groupName);
        group.setCreatedByUsername(createdByUsername);
        group.addMember(createdByUsername);
        return studentGroupRepository.save(group);
    }

    public void joinGroup(Long groupId, String username) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.addMember(username);
        studentGroupRepository.save(group);
    }

    public void leaveGroup(Long groupId, String username) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.removeMember(username);
        studentGroupRepository.save(group);
    }

    public void createRecurringSlots(String instructorUsername,
            LocalDateTime startTime,
            int durationMinutes,
            int breakMinutes,
            int numberOfSlots,
            boolean isGroup,
            int groupCapacity,
            int slotsBeforeLongBreak,
            int longBreakMinutes,
            String repeatType, // "none", "daily", "weekly", "monthly"
            int repeatCount) { // how many times to repeat

        LocalDate semStart = LocalDate.parse(semesterStart);
        LocalDate semEnd = LocalDate.parse(semesterEnd);

        for (int r = 0; r < repeatCount; r++) {
            LocalDateTime batchStart = startTime;

            // Offset each repeat batch by the repeat type
            if (repeatType.equals("daily")) {
                batchStart = startTime.plusDays(r);
            } else if (repeatType.equals("weekly")) {
                batchStart = startTime.plusWeeks(r);
            } else if (repeatType.equals("monthly")) {
                batchStart = startTime.plusMonths(r);
            }

            // Semester check per batch
            if (batchStart.toLocalDate().isBefore(semStart) ||
                    batchStart.toLocalDate().isAfter(semEnd)) {
                continue; // skip batches outside semester silently
            }

            LocalDateTime current = batchStart;
            for (int i = 0; i < numberOfSlots; i++) {
                Appointment appt = new Appointment();
                appt.setInstructorUsername(instructorUsername);
                appt.setSlotTime(current);
                appt.setDurationMinutes(durationMinutes);
                appt.setBooked(false);
                appt.setGroupAppointment(isGroup);
                appt.setGroupCapacity(isGroup ? groupCapacity : 1);
                appt.setCancellationDeadlineHours(24);
                appointmentRepository.save(appt);

                if (slotsBeforeLongBreak > 0 && (i + 1) % slotsBeforeLongBreak == 0) {
                    current = current.plusMinutes(durationMinutes + longBreakMinutes);
                } else {
                    current = current.plusMinutes(durationMinutes + breakMinutes);
                }
            }
        }
    }

}
