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

        long totalUploaded = candidateRepository.count();

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalUploaded", totalUploaded);
        model.addAttribute("totalPresent", 0);
        model.addAttribute("totalAbsent", 0);
        model.addAttribute("totalApproved", 0);
        model.addAttribute("totalRejected", 0);

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
}
