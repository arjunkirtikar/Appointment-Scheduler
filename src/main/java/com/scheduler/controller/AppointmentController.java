package com.scheduler.controller;

import com.scheduler.domain.Appointment;
import com.scheduler.service.AppointmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.scheduler.domain.Appointment;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    // Calendar view — passes ALL appointments for the calendar to render
    @GetMapping("/calendar")
    public String viewCalendar(Model model, Principal principal) {
        model.addAttribute("slots", appointmentService.getAvailableSlots());
        model.addAttribute("allAppointments", appointmentService.getAllAppointments());
        model.addAttribute("groups", appointmentService.getAllGroups());
        return "calendar";
    }

    // Student: book a slot
    @PostMapping("/book/{id}")
    public String bookSlot(@PathVariable Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        Appointment appt = appointmentService.getAppointmentById(id);

        if (appt.isGroupAppointment()) {
            boolean inGroup = appointmentService.isStudentInAnyGroup(principal.getName());
            if (!inGroup) {
                redirectAttributes.addFlashAttribute("groupError",
                        "You must join or create a group before booking a group appointment.");
                return "redirect:/appointments/calendar";
            }
        }

        appointmentService.bookAppointment(id, principal.getName());
        return "redirect:/appointments/my-bookings";
    }

    // Instructor: bulk create slots
    @PostMapping("/create-bulk")
    public String createBulkSlots(
            @RequestParam String startTime,
            @RequestParam int durationMinutes,
            @RequestParam int breakMinutes,
            @RequestParam int numberOfSlots,
            @RequestParam(defaultValue = "false") boolean isGroup,
            @RequestParam(defaultValue = "1") int groupCapacity,
            Principal principal) {

        appointmentService.createBulkSlots(
                principal.getName(),
                LocalDateTime.parse(startTime),
                durationMinutes,
                breakMinutes,
                numberOfSlots,
                isGroup,
                groupCapacity);
        return "redirect:/appointments/calendar";
    }

    // Student: view their bookings
    @GetMapping("/my-bookings")
    public String myBookings(Model model, Principal principal) {
        model.addAttribute("bookings",
                appointmentService.getStudentBookings(principal.getName()));
        return "dashboard-student";
    }

    // Instructor: view their slots + who booked them
    @GetMapping("/my-slots")
    public String mySlots(Model model, Principal principal) {
        model.addAttribute("slots",
                appointmentService.getInstructorSlots(principal.getName()));
        return "dashboard-instructor";
    }

    // Student: create a group
    @PostMapping("/groups/create")
    public String createGroup(@RequestParam String groupName, Principal principal) {
        appointmentService.createGroup(groupName, principal.getName());
        return "redirect:/appointments/calendar";
    }

    // Student: join a group
    @PostMapping("/groups/join/{id}")
    public String joinGroup(@PathVariable Long id, Principal principal) {
        appointmentService.joinGroup(id, principal.getName());
        return "redirect:/appointments/calendar";
    }

    @PostMapping("/groups/leave/{id}")
    public String leaveGroup(@PathVariable Long id, Principal principal) {
        appointmentService.leaveGroup(id, principal.getName());
        return "redirect:/appointments/calendar";
    }

    @GetMapping("/groups/{id}")
    public String viewGroup(@PathVariable Long id, Model model) {
        model.addAttribute("group", appointmentService.getGroupById(id));
        return "group-detail";
    }

}
