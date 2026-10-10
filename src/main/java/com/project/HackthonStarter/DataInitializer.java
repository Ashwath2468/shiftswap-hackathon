
package com.project.HackthonStarter;

import com.project.HackthonStarter.model.Staff;
import com.project.HackthonStarter.model.Shift;
import com.project.HackthonStarter.repository.StaffRepository;
import com.project.HackthonStarter.repository.ShiftRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            StaffRepository staffRepository,
            ShiftRepository shiftRepository) {

        return args -> {

            // Never overwrite existing application data.
            if (staffRepository.count() > 0
                    || shiftRepository.count() > 0) {
                System.out.println(
                        "Existing data found. Demo seeding skipped.");
                return;
            }

            Staff staff1 = new Staff();
            staff1.setId("S1");
            staff1.setName("Ananya");
            staff1.setRole("STAFF");
            staff1.setCertifications(
                    List.of("BASIC", "EMERGENCY"));

            Staff staff2 = new Staff();
            staff2.setId("S2");
            staff2.setName("Rahul");
            staff2.setRole("STAFF");
            staff2.setCertifications(
                    List.of("BASIC"));

            Staff staff3 = new Staff();
            staff3.setId("S3");
            staff3.setName("Priya");
            staff3.setRole("STAFF");
            staff3.setCertifications(
                    List.of("BASIC", "EMERGENCY"));

            Staff manager = new Staff();
            manager.setId("M1");
            manager.setName("Clinic Manager");
            manager.setRole("MANAGER");
            manager.setCertifications(List.of());

            staffRepository.saveAll(
                    List.of(staff1, staff2, staff3, manager));

            // Sample shifts for the week beginning Monday,
            // 12 October 2026. Times are local clinic times.

            Shift shift1 = new Shift();
            shift1.setId("SH1");
            shift1.setStartTime(
                    LocalDateTime.of(2026, 10, 12, 9, 0));
            shift1.setEndTime(
                    LocalDateTime.of(2026, 10, 12, 17, 0));
            shift1.setRequiredCertification("BASIC");
            shift1.setAssignedTo("S1");

            Shift shift2 = new Shift();
            shift2.setId("SH2");
            shift2.setStartTime(
                    LocalDateTime.of(2026, 10, 13, 9, 0));
            shift2.setEndTime(
                    LocalDateTime.of(2026, 10, 13, 17, 0));
            shift2.setRequiredCertification("BASIC");
            shift2.setAssignedTo("S2");

            Shift shift3 = new Shift();
            shift3.setId("SH3");
            shift3.setStartTime(
                    LocalDateTime.of(2026, 10, 14, 9, 0));
            shift3.setEndTime(
                    LocalDateTime.of(2026, 10, 14, 13, 0));
            shift3.setRequiredCertification("EMERGENCY");
            shift3.setAssignedTo("S1");

            Shift shift4 = new Shift();
            shift4.setId("SH4");
            shift4.setStartTime(
                    LocalDateTime.of(2026, 10, 15, 9, 0));
            shift4.setEndTime(
                    LocalDateTime.of(2026, 10, 15, 17, 0));
            shift4.setRequiredCertification(null);
            shift4.setAssignedTo("S3");

            shiftRepository.saveAll(
                    List.of(shift1, shift2, shift3, shift4));

            System.out.println("Demo staff and shifts created.");
        };
    }
}
