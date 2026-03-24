package com.scheduler.controller;

import com.scheduler.domain.Appointment;
import com.scheduler.service.AppointmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/calendar")
    public String viewCalendar(Model model, Principal principal) {
        model.addAttribute("slots", appointmentService.getAvailableSlots());
        model.addAttribute("allAppointments", appointmentService.getAllAppointments());
        model.addAttribute("groups", appointmentService.getAllGroups());
        return "calendar";
    }

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

    @PostMapping("/create-bulk")
    public String createBulkSlots(
            @RequestParam String startTime,
            @RequestParam int durationMinutes,
            @RequestParam int breakMinutes,
            @RequestParam int numberOfSlots,
            @RequestParam(defaultValue = "false") boolean isGroup,
            @RequestParam(defaultValue = "1") int groupCapacity,
            @RequestParam(defaultValue = "0") int slotsBeforeLongBreak,
            @RequestParam(defaultValue = "60") int longBreakMinutes,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.createBulkSlots(
                    principal.getName(),
                    LocalDateTime.parse(startTime),
                    durationMinutes, breakMinutes, numberOfSlots,
                    isGroup, groupCapacity,
                    slotsBeforeLongBreak, longBreakMinutes);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("slotError", e.getMessage());
        }
        return "redirect:/appointments/calendar";
    }

    @PostMapping("/create-recurring")
    public String createRecurringSlots(
            @RequestParam String startTime,
            @RequestParam int durationMinutes,
            @RequestParam int breakMinutes,
            @RequestParam int numberOfSlots,
            @RequestParam(defaultValue = "false") boolean isGroup,
            @RequestParam(defaultValue = "1") int groupCapacity,
            @RequestParam(defaultValue = "0") int slotsBeforeLongBreak,
            @RequestParam(defaultValue = "60") int longBreakMinutes,
            @RequestParam(defaultValue = "none") String repeatType,
            @RequestParam(defaultValue = "1") int repeatCount,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.createRecurringSlots(
                    principal.getName(),
                    LocalDateTime.parse(startTime),
                    durationMinutes, breakMinutes, numberOfSlots,
                    isGroup, groupCapacity,
                    slotsBeforeLongBreak, longBreakMinutes,
                    repeatType, repeatCount);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("slotError", e.getMessage());
        }
        return "redirect:/appointments/calendar";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, Principal principal) {
        model.addAttribute("bookings",
                appointmentService.getStudentBookings(principal.getName()));
        return "dashboard-student";
    }

    @GetMapping("/my-slots")
    public String mySlots(Model model, Principal principal) {
        model.addAttribute("slots",
                appointmentService.getInstructorSlots(principal.getName()));
        return "dashboard-instructor";
    }

    @PostMapping("/cancel/student/{id}")
    public String cancelByStudent(@PathVariable Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByStudent(id, principal.getName());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("cancelError", e.getMessage());
        }
        return "redirect:/appointments/my-bookings";
    }

    @PostMapping("/cancel/instructor/{id}")
    public String cancelByInstructor(@PathVariable Long id, Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelByInstructor(id, principal.getName());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("cancelError", e.getMessage());
        }
        return "redirect:/appointments/my-slots";
    }

    @GetMapping("/groups")
    public String groupsPage(Model model, Principal principal) {
        model.addAttribute("groups", appointmentService.getAllGroups());
        model.addAttribute("currentUser", principal.getName());
        return "groups";
    }

    @PostMapping("/groups/create")
    public String createGroup(@RequestParam String groupName, Principal principal) {
        appointmentService.createGroup(groupName, principal.getName());
        return "redirect:/appointments/groups";
    }

    @PostMapping("/groups/join/{id}")
    public String joinGroup(@PathVariable Long id, Principal principal) {
        appointmentService.joinGroup(id, principal.getName());
        return "redirect:/appointments/groups";
    }

    @PostMapping("/groups/leave/{id}")
    public String leaveGroup(@PathVariable Long id, Principal principal) {
        appointmentService.leaveGroup(id, principal.getName());
        return "redirect:/appointments/groups";
    }

    @GetMapping("/groups/{id}")
    public String viewGroup(@PathVariable Long id, Model model) {
        model.addAttribute("group", appointmentService.getGroupById(id));
        return "group-detail";
    }
}
