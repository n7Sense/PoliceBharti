package com.nst.ufrs.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String genericDashboard(Model model, HttpSession httpSession) {

        long userID = (Long) httpSession.getAttribute("userID");
        String name = (String) httpSession.getAttribute("name");
        String email = (String) httpSession.getAttribute("email");

        model.addAttribute("activePage", "dashboard");
        model.addAttribute("totalUploaded", 100);
        model.addAttribute("totalPresent", 250);
        model.addAttribute("totalAbsent", 350);
        model.addAttribute("totalApproved", 25);
        model.addAttribute("totalRejected", 36);

        return "dashboard";
    }

    @GetMapping("/upload-candidate")
    public String uploadCandidate(Model model) {

        return "upload-candidate";
    }
}
