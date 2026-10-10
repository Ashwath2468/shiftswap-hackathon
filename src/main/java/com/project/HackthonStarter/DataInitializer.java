
package com.project.HackthonStarter;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.project.HackthonStarter.model.Shift;
import com.project.HackthonStarter.model.Staff;
import com.project.HackthonStarter.repository.ActionHistoryRepository;
import com.project.HackthonStarter.repository.ListingRepository;
import com.project.HackthonStarter.repository.ShiftRepository;
import com.project.HackthonStarter.repository.StaffRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initializeData(
            StaffRepository staffRepository,
            ShiftRepository shiftRepository,
            ListingRepository listingRepository,
            ActionHistoryRepository historyRepository,
            JsonMapper jsonMapper) {

        return args -> {

            // Preserve all existing application data.
            if (staffRepository.count() > 0
                    || shiftRepository.count() > 0
                    || listingRepository.count() > 0
                    || historyRepository.count() > 0) {

                System.out.println(
                        "Existing data found. seed.json loading skipped.");
                return;
            }

            List<Staff> staffList = new ArrayList<>();
            List<Shift> shiftList = new ArrayList<>();

            try (InputStream input =
                    new ClassPathResource("seed.json").getInputStream()) {

                JsonNode root = jsonMapper.readTree(input);

                if (root == null
                        || !root.has("staff")
                        || !root.has("shifts")
                        || !root.get("staff").isArray()
                        || !root.get("shifts").isArray()) {

                    throw new IllegalArgumentException(
                            "seed.json must contain staff and shifts arrays.");
                }

                // Load staff members.
                for (JsonNode node : root.path("staff")) {

                    Staff staff = new Staff();

                    staff.setId(node.path("id").asText());
                    staff.setName(node.path("name").asText());
                    staff.setRole(node.path("role").asText());

                    List<String> certifications = new ArrayList<>();

                    for (JsonNode cert : node.path("certifications")) {
                        certifications.add(cert.asText());
                    }

                    staff.setCertifications(certifications);
                    staffList.add(staff);
                }

                // Load shifts using the original JSON field names.
                for (JsonNode node : root.path("shifts")) {

                    Shift shift = new Shift();

                    shift.setId(node.path("id").asText());

                    shift.setStartTime(
                            LocalDateTime.parse(
                                    node.path("start").asText()));

                    shift.setEndTime(
                            LocalDateTime.parse(
                                    node.path("end").asText()));

                    JsonNode requiredCert =
                            node.path("required_certification");

                    shift.setRequiredCertification(
                            requiredCert.isNull()
                                    ? null
                                    : requiredCert.asText());

                    shift.setAssignedTo(
                            node.path("assigned_to").asText());

                    shiftList.add(shift);
                }

                // Save parsed data only after the file has been read.
                staffRepository.saveAll(staffList);
                shiftRepository.saveAll(shiftList);

                System.out.println(
                        "Successfully loaded " + staffList.size()
                                + " staff members and "
                                + shiftList.size()
                                + " shifts from seed.json.");

            } catch (Exception e) {

                throw new IllegalStateException(
                        "Failed to load seed.json. Check the file "
                                + "location, JSON structure, and field values.",
                        e);
            }
        };
    }
}
