package com.scheduler;

import com.scheduler.domain.Appointment;
import com.scheduler.domain.StudentGroup;
import com.scheduler.repository.AppointmentRepository;
import com.scheduler.repository.StudentGroupRepository;
import com.scheduler.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    AppointmentRepository appointmentRepository;

    @Mock
    StudentGroupRepository studentGroupRepository;

    @InjectMocks
    AppointmentService appointmentService;

    // Test 1: Booking marks slot as booked and assigns student
    @Test
    void bookAppointment_shouldMarkSlotAsBooked() {
        Appointment slot = new Appointment();
        slot.setId(1L);
        slot.setBooked(false);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(slot));

        appointmentService.bookAppointment(1L, "student1");

        assertTrue(slot.isBooked());
        assertEquals("student1", slot.getStudentUsername());
    }

    // Test 2: Cannot book an already booked slot
    @Test
    void bookAppointment_shouldThrowIfAlreadyBooked() {
        Appointment slot = new Appointment();
        slot.setId(1L);
        slot.setBooked(true);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(slot));

        assertThrows(RuntimeException.class, () ->
            appointmentService.bookAppointment(1L, "student1"));
    }

    // Test 3: Bulk slot creation generates correct number of slots
    @Test
    void createBulkSlots_shouldCreateCorrectNumberOfSlots() {
        appointmentService.createBulkSlots(
            "prof1",
            LocalDateTime.now(),
            15, 5, 4,
            false, 1
        );
        verify(appointmentRepository, times(4)).save(any(Appointment.class));
    }

    // Test 4: Bulk slots have correct time spacing
    @Test
    void createBulkSlots_shouldSpaceSlotsByDurationPlusBreak() {
        LocalDateTime start = LocalDateTime.of(2026, 3, 10, 9, 0);
        appointmentService.createBulkSlots("prof1", start, 15, 5, 2, false, 1);

        // Capture saved appointments
        var captor = org.mockito.ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository, times(2)).save(captor.capture());

        List<Appointment> saved = captor.getAllValues();
        assertEquals(start, saved.get(0).getSlotTime());
        assertEquals(start.plusMinutes(20), saved.get(1).getSlotTime()); // 15 + 5
    }

    // Test 5: Student in a group returns true
    @Test
    void isStudentInAnyGroup_shouldReturnTrueIfInGroup() {
        StudentGroup group = new StudentGroup();
        group.setMemberUsernames("student1");

        when(studentGroupRepository.findByMemberUsernamesContaining("student1"))
            .thenReturn(List.of(group));

        assertTrue(appointmentService.isStudentInAnyGroup("student1"));
    }

    // Test 6: Student NOT in any group returns false
    @Test
    void isStudentInAnyGroup_shouldReturnFalseIfNotInGroup() {
        when(studentGroupRepository.findByMemberUsernamesContaining("student1"))
            .thenReturn(List.of());

        assertFalse(appointmentService.isStudentInAnyGroup("student1"));
    }

    // Test 7: Creating a group saves it with the creator as member
    @Test
    void createGroup_shouldSaveGroupWithCreatorAsMember() {
        StudentGroup saved = new StudentGroup();
        saved.setGroupName("Team A");
        saved.setMemberUsernames("student1");

        when(studentGroupRepository.save(any())).thenReturn(saved);

        StudentGroup result = appointmentService.createGroup("Team A", "student1");

        assertEquals("Team A", result.getGroupName());
        assertTrue(result.getMemberUsernames().contains("student1"));
    }
}
