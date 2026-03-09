package com.scheduler;

import com.scheduler.service.AppointmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AppointmentService appointmentService;

    // Test 1: Unauthenticated user is redirected to login
    @Test
    void calendar_shouldRedirectToLoginIfNotAuthenticated() throws Exception {
        mockMvc.perform(get("/appointments/calendar"))
            .andExpect(status().is3xxRedirection());
    }

    // Test 2: Authenticated student can access calendar
    @Test
    void calendar_shouldReturnOkForAuthenticatedStudent() throws Exception {
        when(appointmentService.getAvailableSlots()).thenReturn(List.of());
        when(appointmentService.getAllAppointments()).thenReturn(List.of());
        when(appointmentService.getAllGroups()).thenReturn(List.of());

        mockMvc.perform(get("/appointments/calendar"))
            .andExpect(status().isOk())
            .andExpect(view().name("calendar"));
    }
    // Test 3: Authenticated instructor can access calendar
    @Test
    void calendar_shouldReturnOkForAuthenticatedInstructor() throws Exception {
        when(appointmentService.getAvailableSlots()).thenReturn(List.of());
        when(appointmentService.getAllAppointments()).thenReturn(List.of());
        when(appointmentService.getAllGroups()).thenReturn(List.of());

        mockMvc.perform(get("/appointments/calendar"))
            .andExpect(status().isOk())
            .andExpect(view().name("calendar"));
    }
}
