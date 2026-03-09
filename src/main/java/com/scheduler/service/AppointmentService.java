package com.scheduler.service;

import com.scheduler.domain.Appointment;
import com.scheduler.domain.StudentGroup;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.StudentGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final StudentGroupRepository studentGroupRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
            StudentGroupRepository studentGroupRepository) {
        this.appointmentRepository = appointmentRepository;
        this.studentGroupRepository = studentGroupRepository;
    }

    // ── Individual Slots ──────────────────────────────────────────

    public List<Appointment> getAvailableSlots() {
        return appointmentRepository.findByIsBookedFalse();
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getStudentBookings(String username) {
        return appointmentRepository.findByStudentUsername(username);
    }

    public List<Appointment> getInstructorSlots(String username) {
        return appointmentRepository.findByInstructorUsername(username);
    }

    public void bookAppointment(Long id, String studentUsername) {
        Appointment appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        if (appt.isBooked())
            throw new RuntimeException("Already booked");
        appt.setBooked(true);
        appt.setStudentUsername(studentUsername);
        appointmentRepository.save(appt);
    }

    // ── Bulk Slot Creation ────────────────────────────────────────
    // Creates multiple slots given a start time, slot duration, break duration,
    // number of slots, and whether it is a group appointment

    public void createBulkSlots(String instructorUsername,
            LocalDateTime startTime,
            int durationMinutes,
            int breakMinutes,
            int numberOfSlots,
            boolean isGroup,
            int groupCapacity) {

        LocalDateTime current = startTime;
        for (int i = 0; i < numberOfSlots; i++) {
            Appointment appt = new Appointment();
            appt.setInstructorUsername(instructorUsername);
            appt.setSlotTime(current);
            appt.setDurationMinutes(durationMinutes);
            appt.setBooked(false);
            appt.setGroupAppointment(isGroup);
            appt.setGroupCapacity(isGroup ? groupCapacity : 1);
            appointmentRepository.save(appt);
            // Advance time by slot duration + break
            current = current.plusMinutes(durationMinutes + breakMinutes);
        }
    }

    // ── Group Management ──────────────────────────────────────────

    public StudentGroup createGroup(String groupName, String createdByUsername) {
        StudentGroup group = new StudentGroup();
        group.setGroupName(groupName);
        group.setCreatedByUsername(createdByUsername);
        group.addMember(createdByUsername);
        return studentGroupRepository.save(group);
    }

    public List<StudentGroup> getAllGroups() {
        return studentGroupRepository.findAll();
    }

    public List<StudentGroup> getGroupsForStudent(String username) {
        return studentGroupRepository.findByMemberUsernamesContaining(username);
    }

    public void joinGroup(Long groupId, String username) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.addMember(username);
        studentGroupRepository.save(group);
    }

    // ── Group Appointments ────────────────────────────────────────

    public List<Appointment> getGroupAppointments() {
        return appointmentRepository.findByIsGroupAppointmentTrue();
    }

    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public boolean isStudentInAnyGroup(String username) {
        List<StudentGroup> groups = studentGroupRepository
                .findByMemberUsernamesContaining(username);
        return !groups.isEmpty();
    }

    public void leaveGroup(Long groupId, String username) {
        StudentGroup group = studentGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        group.removeMember(username);
        studentGroupRepository.save(group);
    }

}
