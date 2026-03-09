package com.scheduler.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String instructorUsername;
    private LocalDateTime slotTime;
    private boolean isBooked;
    private String studentUsername;
    private int durationMinutes;
    private boolean isGroupAppointment;
    private int groupCapacity;

    private Long groupId;

    public Appointment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInstructorUsername() { return instructorUsername; }
    public void setInstructorUsername(String u) { this.instructorUsername = u; }

    public LocalDateTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalDateTime t) { this.slotTime = t; }

    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean b) { this.isBooked = b; }

    public String getStudentUsername() { return studentUsername; }
    public void setStudentUsername(String u) { this.studentUsername = u; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int d) { this.durationMinutes = d; }

    public boolean isGroupAppointment() { return isGroupAppointment; }
    public void setGroupAppointment(boolean g) { this.isGroupAppointment = g; }

    public int getGroupCapacity() { return groupCapacity; }
    public void setGroupCapacity(int c) { this.groupCapacity = c; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}
