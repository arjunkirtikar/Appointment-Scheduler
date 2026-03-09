package com.scheduler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

// Needs to mark this class as a source of Spring beam def
// Replaces old XML config files
// Activates Spring Security

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**").permitAll()
                .requestMatchers("/appointments/create").hasRole("INSTRUCTOR")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/appointments/calendar", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService users() {
        UserDetails student = User.withDefaultPasswordEncoder()
            .username("student1").password("pass").roles("STUDENT").build();
        UserDetails instructor = User.withDefaultPasswordEncoder()
            .username("prof1").password("pass").roles("INSTRUCTOR").build();
        return new InMemoryUserDetailsManager(student, instructor);
    }
}
