

package com.project.HackthonStarter.service;

import com.project.HackthonStarter.model.Listing;
import com.project.HackthonStarter.model.Shift;
import com.project.HackthonStarter.model.Staff;
import com.project.HackthonStarter.repository.ListingRepository;
import com.project.HackthonStarter.repository.ShiftRepository;
import com.project.HackthonStarter.repository.StaffRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ShiftService {

    private final StaffRepository staffRepository;
    private final ShiftRepository shiftRepository;
    private final ListingRepository listingRepository;

    public ShiftService(
            StaffRepository staffRepository,
            ShiftRepository shiftRepository,
            ListingRepository listingRepository) {

        this.staffRepository = staffRepository;
        this.shiftRepository = shiftRepository;
        this.listingRepository = listingRepository;
    }

    // =========================================================
    // CLAIM ELIGIBILITY PREVIEW
    // Does not change the listing or reassign the shift.
    // =========================================================

    @Transactional(readOnly = true)
    public List<String> getClaimErrors(
            String listingId, String claimantId) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        // Availability must be checked before other claim rules.
        if (!"OPEN".equals(listing.getStatus())) {
            return List.of("Listing is no longer available");
        }

        Shift shift = shiftRepository.findById(listing.getShiftId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Shift not found"));

        Staff claimant = staffRepository.findById(claimantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Staff member not found"));

        if ("MANAGER".equals(claimant.getRole())) {
            return List.of("Managers cannot claim shifts");
        }

        // Rule 1: Only report this rule when claiming one's own shift.
        if (claimantId.equals(shift.getAssignedTo())) {
            return List.of(
                    "Rule 1 violated: You cannot claim your own shift");
        }

        return validateClaimRules(shift, claimantId);
    }

    // =========================================================
    // CLAIM A SHIFT
    // An empty result means success.
    // =========================================================

    @Transactional
    public List<String> claimShift(
            String listingId, String claimantId) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        // Recheck availability at the time of the actual claim.
        if (!"OPEN".equals(listing.getStatus())) {
            return List.of("Listing is no longer available");
        }

        List<String> errors = getClaimErrors(listingId, claimantId);

        if (!errors.isEmpty()) {
            return errors;
        }

        listing.setClaimerId(claimantId);
        listing.setStatus("CLAIMED");
        listingRepository.save(listing);

        // The original owner remains assigned until manager approval.
        return List.of();
    }

    // =========================================================
    // VALIDATE RULES 2–5
    // Shared by eligibility preview, claiming, and approval.
    // =========================================================

    private List<String> validateClaimRules(
            Shift requestedShift, String claimantId) {

        List<String> errors = new ArrayList<>();

        Staff claimant = staffRepository.findById(claimantId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Staff member not found"));

        if ("MANAGER".equals(claimant.getRole())) {
            errors.add("Managers cannot claim shifts");
            return errors;
        }

        // Rule 2: Required certification.
        String required = requestedShift.getRequiredCertification();

        if (required != null && !required.isBlank()
                && (claimant.getCertifications() == null
                || !claimant.getCertifications().contains(required))) {

            errors.add(
                    "Rule 2 violated: Missing certification " + required);
        }

        // Only currently assigned shifts count.
        // Pending claims do not count as assignments.
        List<Shift> assignedShifts = shiftRepository.findAll()
                .stream()
                .filter(s -> claimantId.equals(s.getAssignedTo()))
                .filter(s -> !s.getId().equals(requestedShift.getId()))
                .toList();

        LocalDateTime requestedStart = requestedShift.getStartTime();
        LocalDateTime requestedEnd = requestedShift.getEndTime();

        // Rules 3 and 4: Overlap and minimum rest.
        for (Shift existing : assignedShifts) {

            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existing.getEndTime();

            // Rule 3: A and B overlap iff A.start < B.end
            // and B.start < A.end.
            boolean overlaps = requestedStart.isBefore(existingEnd)
                    && existingStart.isBefore(requestedEnd);

            if (overlaps) {
                errors.add(
                        "Rule 3 violated: Overlaps with shift "
                                + existing.getId());

                // An overlapping shift has no separate rest gap.
                continue;
            }

            // Rule 4: At least 11 hours of rest.
            Duration rest;

            if (!existingEnd.isAfter(requestedStart)) {
                // Existing shift occurs before requested shift.
                rest = Duration.between(existingEnd, requestedStart);
            } else {
                // Existing shift occurs after requested shift.
                rest = Duration.between(requestedEnd, existingStart);
            }

            // Exactly 11 hours is allowed.
            if (rest.compareTo(Duration.ofHours(11)) < 0) {
                errors.add(
                        "Rule 4 violated: Less than 11 hours of rest "
                                + "between shift " + existing.getId()
                                + " and the requested shift");
            }
        }

        // Rule 5: Maximum 40 assigned hours in the week
        // in which the requested shift starts.
        LocalDate monday = requestedStart.toLocalDate()
                .with(DayOfWeek.MONDAY);

        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime nextWeek = monday.plusDays(7).atStartOfDay();

        long assignedMinutes = assignedShifts.stream()
                .filter(s -> !s.getStartTime().isBefore(weekStart)
                        && s.getStartTime().isBefore(nextWeek))
                .mapToLong(s -> Duration.between(
                        s.getStartTime(), s.getEndTime()).toMinutes())
                .sum();

        long requestedMinutes = Duration.between(
                requestedStart, requestedEnd).toMinutes();

        if (assignedMinutes + requestedMinutes > 40 * 60) {
            errors.add(
                    "Rule 5 violated: Weekly hours would exceed 40");
        }

        return errors;
    }

    // =========================================================
    // POST A SHIFT
    // =========================================================

    @Transactional
    public String postShift(String shiftId, String posterId) {

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Shift not found"));

        Staff poster = staffRepository.findById(posterId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Staff not found"));

        if (!"STAFF".equals(poster.getRole())) {
            throw new IllegalArgumentException(
                    "Only staff can post shifts");
        }

        if (!posterId.equals(shift.getAssignedTo())) {
            throw new IllegalArgumentException(
                    "You do not own this shift");
        }

        boolean alreadyListed = listingRepository.findByShiftId(shiftId)
                .stream()
                .anyMatch(l -> "OPEN".equals(l.getStatus())
                        || "CLAIMED".equals(l.getStatus()));

        if (alreadyListed) {
            throw new IllegalArgumentException(
                    "Shift already has an active listing");
        }

        Listing listing = new Listing();
        listing.setId(UUID.randomUUID().toString());
        listing.setShiftId(shiftId);
        listing.setPosterId(posterId);
        listing.setClaimerId(null);
        listing.setStatus("OPEN");

        listingRepository.save(listing);

        return listing.getId();
    }

    // =========================================================
    // WITHDRAW AN OPEN LISTING
    // =========================================================

    @Transactional
    public void withdrawListing(String listingId, String posterId) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        if (!posterId.equals(listing.getPosterId())) {
            throw new IllegalArgumentException(
                    "Only the poster can withdraw");
        }

        if (!"OPEN".equals(listing.getStatus())) {
            throw new IllegalArgumentException(
                    "Only OPEN listings can be withdrawn");
        }

        listing.setStatus("WITHDRAWN");
        listingRepository.save(listing);
    }

    // =========================================================
    // UNCLAIM A CLAIMED LISTING
    // =========================================================

    @Transactional
    public void unclaimListing(String listingId, String claimantId) {

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        if (!"CLAIMED".equals(listing.getStatus())) {
            throw new IllegalArgumentException(
                    "Listing is not CLAIMED");
        }

        if (!claimantId.equals(listing.getClaimerId())) {
            throw new IllegalArgumentException(
                    "Only the claimant can unclaim");
        }

        listing.setClaimerId(null);
        listing.setStatus("OPEN");

        listingRepository.save(listing);
    }

    // =========================================================
    // APPROVE A CLAIMED LISTING
    // Recheck Rules 2–5 before reassignment.
    // =========================================================

    @Transactional
    public void approveListing(String listingId, String managerId) {

        Staff manager = staffRepository.findById(managerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Staff not found"));

        if (!"MANAGER".equals(manager.getRole())) {
            throw new IllegalArgumentException(
                    "Only managers can approve");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        if (!"CLAIMED".equals(listing.getStatus())) {
            throw new IllegalArgumentException(
                    "Listing must be CLAIMED");
        }

        String claimantId = listing.getClaimerId();

        if (claimantId == null || claimantId.isBlank()) {
            throw new IllegalArgumentException("Claimant is missing");
        }

        Shift shift = shiftRepository.findById(listing.getShiftId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Shift not found"));

        // Rule 1 was checked during claiming.
        // Revalidate certification, overlap, rest, and weekly hours.
        List<String> errors = validateClaimRules(shift, claimantId);

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(
                    String.join("; ", errors));
        }

        // Reassign only after successful validation.
        shift.setAssignedTo(claimantId);
        shiftRepository.save(shift);

        listing.setStatus("APPROVED");
        listingRepository.save(listing);
    }

    // =========================================================
    // REJECT A CLAIMED LISTING
    // =========================================================

    @Transactional
    public void rejectListing(String listingId, String managerId) {

        Staff manager = staffRepository.findById(managerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Staff not found"));

        if (!"MANAGER".equals(manager.getRole())) {
            throw new IllegalArgumentException(
                    "Only managers can reject");
        }

        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Listing not found"));

        if (!"CLAIMED".equals(listing.getStatus())) {
            throw new IllegalArgumentException(
                    "Listing must be CLAIMED");
        }

        listing.setClaimerId(null);
        listing.setStatus("OPEN");

        listingRepository.save(listing);
    }
}
