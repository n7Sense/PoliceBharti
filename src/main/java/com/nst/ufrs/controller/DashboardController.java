package com.nst.ufrs.controller;

import com.nst.ufrs.repository.CandidateRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final CandidateRepository candidateRepository;

    @GetMapping("/dashboard")
    public String genericDashboard(Model model, HttpSession httpSession) {

        Long userID = (Long) httpSession.getAttribute("userID");
        String name = (String) httpSession.getAttribute("name");
        String email = (String) httpSession.getAttribute("email");
        Long eventLocationId = (Long) httpSession.getAttribute("eventLocationId");

        long totalUploaded = candidateRepository.count();
        long totalPresent = candidateRepository.findAllByAttendance(true).size();
        long totalAbsent = candidateRepository.findAllByAttendance(false).size();
        long totalApproved = candidateRepository.findAllByStatus(true).size();
        long totalRejected = candidateRepository.findAllByStatus(false).size();

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalUploaded", totalUploaded);
        model.addAttribute("totalPresent", totalPresent);
        model.addAttribute("totalAbsent", totalAbsent);
        model.addAttribute("totalApproved", totalApproved);
        model.addAttribute("totalRejected", totalRejected);

        return "dashboard";
    }

    @GetMapping("/upload-candidate")
    public String uploadCandidate(Model model) {
        return "upload-candidate";
    }

    @GetMapping("/add-candidate")
    public String addCandidate(Model model) {
        return "add-candidate";
    }

    @GetMapping("/physical-test")
    public String physicalTest(Model model) {
        return "physical-test";
    }

    @GetMapping("/appeal-1")
    public String appeal1(Model model) {
        return "appeal-1";
    }

    @GetMapping("/appeal-2")
    public String appeal2(Model model) {
        return "appeal-2";
    }

    @GetMapping("/assign-running-number")
    public String assignRunningNumber(Model model) {
        model.addAttribute("activePage", "assignRunningNumber");
        return "assign-running-number";
    }

    @GetMapping("/batch-master")
    public String batchMaster(Model model) {
        model.addAttribute("activePage", "batchMaster");
        return "batch-master";
    }

    @GetMapping("/assign-chest-number")
    public String assignChestNumber(Model model) {
        model.addAttribute("activePage", "assignChest");
        return "assign-chest-number";
    }
}
