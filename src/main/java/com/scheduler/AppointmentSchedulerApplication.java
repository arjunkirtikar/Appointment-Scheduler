package com.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Bootstraps the app
// Activates auto-configuration and scans all sub-packages for Spring-managed beans

@SpringBootApplication
public class AppointmentSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppointmentSchedulerApplication.class, args);
    }
}
