package com.scheduler.repository;

import com.scheduler.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByIsBookedFalse();
    List<Appointment> findByStudentUsername(String username);
    List<Appointment> findByInstructorUsername(String username);
    List<Appointment> findByIsGroupAppointmentTrue();
    List<Appointment> findByGroupId(Long groupId);
}
