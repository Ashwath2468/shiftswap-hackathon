



package com.project.HackthonStarter.controller;

import com.project.HackthonStarter.model.ActionHistory;
import com.project.HackthonStarter.model.Listing;
import com.project.HackthonStarter.model.Staff;
import com.project.HackthonStarter.repository.ActionHistoryRepository;
import com.project.HackthonStarter.repository.ListingRepository;
import com.project.HackthonStarter.repository.ShiftRepository;
import com.project.HackthonStarter.repository.StaffRepository;
import com.project.HackthonStarter.service.ShiftService;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class ShiftSwapController {

    private final StaffRepository staffRepository;
    private final ShiftRepository shiftRepository;
    private final ListingRepository listingRepository;
    private final ActionHistoryRepository actionHistoryRepository;
    private final ShiftService shiftService;

    public ShiftSwapController(
            StaffRepository staffRepository,
            ShiftRepository shiftRepository,
            ListingRepository listingRepository,
            ActionHistoryRepository actionHistoryRepository,
            ShiftService shiftService) {

        this.staffRepository = staffRepository;
        this.shiftRepository = shiftRepository;
        this.listingRepository = listingRepository;
        this.actionHistoryRepository = actionHistoryRepository;
        this.shiftService = shiftService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String actorId,
            Model model) {

        var staffMembers = staffRepository.findAll();

        String selectedId = actorId;
        Staff selectedActor = null;

        if (selectedId != null && !selectedId.isBlank()) {
            for (Staff person : staffMembers) {
                if (person.getId().equals(selectedId)) {
                    selectedActor = person;
                    break;
                }
            }
        }

        if (selectedActor == null && !staffMembers.isEmpty()) {
            selectedActor = staffMembers.get(0);
            selectedId = selectedActor.getId();
        }

        var shifts = shiftRepository.findAll();
        var listings = listingRepository.findAll();

        Map<String, List<String>> claimErrors = new HashMap<>();

        if (selectedActor != null
                && !"MANAGER".equals(selectedActor.getRole())) {
            for (Listing listing : listings) {
                if ("OPEN".equals(listing.getStatus())) {
                    try {
                        List<String> errors = shiftService.getClaimErrors(
                                listing.getId(), selectedActor.getId());

                        claimErrors.put(listing.getId(), errors);
                    } catch (IllegalArgumentException ex) {
                        claimErrors.put(
                                listing.getId(),
                                List.of(ex.getMessage()));
                    }
                }
            }
        } else {
            for (Listing listing : listings) {
                if ("OPEN".equals(listing.getStatus())) {
                    claimErrors.put(
                            listing.getId(),
                            List.of("Managers cannot claim shifts"));
                }
            }
        }

        model.addAttribute("staffMembers", staffMembers);
        model.addAttribute("shifts", shifts);
        model.addAttribute("listings", listings);
        model.addAttribute("selectedActorId", selectedId);
        model.addAttribute("selectedActor", selectedActor);
        model.addAttribute("claimErrors", claimErrors);
        model.addAttribute(
                "actionHistory",
                actionHistoryRepository.findAllByOrderByTimestampDesc());

        return "index";
    }

    @PostMapping("/post")
    public String postShift(
            @RequestParam String shiftId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        try {
            shiftService.postShift(shiftId, actorId);

            record(actorId, "POST", shiftId, "SUCCESS",
                    "Shift listing created successfully.");

            redirect.addFlashAttribute(
                    "successMessage", "Shift posted successfully.");

        } catch (IllegalArgumentException ex) {
            record(actorId, "POST", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    @PostMapping("/claim")
    public String claimShift(
            @RequestParam String listingId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        String shiftId = findShiftId(listingId);

        try {
            List<String> errors = shiftService.claimShift(listingId, actorId);

            if (errors.isEmpty()) {
                record(actorId, "CLAIM", shiftId, "SUCCESS",
                        "Claim submitted; awaiting manager approval.");

                redirect.addFlashAttribute(
                        "successMessage",
                        "Shift claimed. Awaiting manager approval.");
            } else {
                String details = String.join("; ", errors);

                record(actorId, "CLAIM", shiftId, "FAILED", details);
                redirect.addFlashAttribute("errorMessage", details);
            }

        } catch (IllegalArgumentException ex) {
            record(actorId, "CLAIM", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    @PostMapping("/withdraw")
    public String withdraw(
            @RequestParam String listingId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        String shiftId = findShiftId(listingId);

        try {
            shiftService.withdrawListing(listingId, actorId);

            record(actorId, "WITHDRAW", shiftId, "SUCCESS",
                    "Shift listing withdrawn.");

            redirect.addFlashAttribute(
                    "successMessage", "Listing withdrawn.");

        } catch (IllegalArgumentException ex) {
            record(actorId, "WITHDRAW", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    @PostMapping("/unclaim")
    public String unclaim(
            @RequestParam String listingId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        String shiftId = findShiftId(listingId);

        try {
            shiftService.unclaimListing(listingId, actorId);

            record(actorId, "UNCLAIM", shiftId, "SUCCESS",
                    "Claim cancelled; listing reopened.");

            redirect.addFlashAttribute(
                    "successMessage",
                    "Claim cancelled. Listing is open again.");

        } catch (IllegalArgumentException ex) {
            record(actorId, "UNCLAIM", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    @PostMapping("/approve")
    public String approve(
            @RequestParam String listingId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        String shiftId = findShiftId(listingId);

        try {
            shiftService.approveListing(listingId, actorId);

            record(actorId, "APPROVE", shiftId, "SUCCESS",
                    "Manager approved the shift claim.");

            redirect.addFlashAttribute(
                    "successMessage", "Shift swap approved.");

        } catch (IllegalArgumentException ex) {
            record(actorId, "APPROVE", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    @PostMapping("/reject")
    public String reject(
            @RequestParam String listingId,
            @RequestParam String actorId,
            RedirectAttributes redirect) {

        String shiftId = findShiftId(listingId);

        try {
            shiftService.rejectListing(listingId, actorId);

            record(actorId, "REJECT", shiftId, "SUCCESS",
                    "Manager rejected the shift claim.");

            redirect.addFlashAttribute(
                    "successMessage",
                    "Claim rejected. Listing is open again.");

        } catch (IllegalArgumentException ex) {
            record(actorId, "REJECT", shiftId, "FAILED", ex.getMessage());
            redirect.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return redirectToHome(actorId);
    }

    private String findShiftId(String listingId) {
        if (listingId == null) {
            return "UNKNOWN";
        }

        return listingRepository.findById(listingId)
                .map(Listing::getShiftId)
                .orElse("UNKNOWN");
    }

    @Transactional
    protected void record(
            String actorId,
            String action,
            String shiftId,
            String outcome,
            String details) {

        ActionHistory event = new ActionHistory();

        event.setId(UUID.randomUUID().toString());
        event.setActorId(actorId == null ? "UNKNOWN" : actorId);
        event.setAction(action);
        event.setShiftId(shiftId == null ? "UNKNOWN" : shiftId);
        event.setOutcome(outcome);
        event.setDetails(details);
        event.setTimestamp(LocalDateTime.now());

        actionHistoryRepository.save(event);
    }

    private String redirectToHome(String actorId) {
        if (actorId == null || actorId.isBlank()) {
            return "redirect:/";
        }

        return "redirect:/?actorId=" + actorId;
    }
}
