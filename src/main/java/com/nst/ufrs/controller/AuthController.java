package com.nst.ufrs.controller;

import com.nst.ufrs.dto.ForgotPasswordDto;
import com.nst.ufrs.dto.LoginDto;
import com.nst.ufrs.dto.RegistrationDto;
import com.nst.ufrs.dto.ResetPasswordDto;
import com.nst.ufrs.entity.PasswordResetToken;
import com.nst.ufrs.entity.User;
import com.nst.ufrs.service.PasswordResetTokenService;
import com.nst.ufrs.service.UserService;
import jakarta.persistence.EntityExistsException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;

    public AuthController(UserService userService,
                          PasswordResetTokenService passwordResetTokenService) {
        this.userService = userService;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        model.addAttribute("loginDto", new LoginDto());
        model.addAttribute("error", error != null);
        model.addAttribute("logout", logout != null);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("registrationDto") @Valid RegistrationDto registrationDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (!bindingResult.hasErrors() && !registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(registrationDto);
        } catch (EntityExistsException ex) {
            bindingResult.rejectValue("email", "email.exists", ex.getMessage());
            return "register";
        } catch (ValidationException ex) {
            bindingResult.reject("validation.error", ex.getMessage());
            return "register";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("role", "role.invalid", ex.getMessage());
            return "register";
        }

        model.addAttribute("successMessage", "Registration successful. You can now login.");
        model.addAttribute("registrationDto", new RegistrationDto());
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @ModelAttribute("forgotPasswordDto") @Valid ForgotPasswordDto forgotPasswordDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        Optional<User> userOpt = userService.findByEmail(forgotPasswordDto.getEmail());
        if (userOpt.isEmpty()) {
            bindingResult.rejectValue("email", "email.notfound", "No account found with this email");
            return "forgot-password";
        }

        PasswordResetToken token = passwordResetTokenService.createTokenForUser(userOpt.get());
        String resetLink = "/reset-password?token=" + token.getToken();
        model.addAttribute("infoMessage", "Password reset link (for demo): " + resetLink);

        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenService.validatePasswordResetToken(token);
        if (tokenOpt.isEmpty()) {
            model.addAttribute("tokenError", "Invalid or expired token");
            return "reset-password";
        }

        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setToken(token);
        model.addAttribute("resetPasswordDto", resetPasswordDto);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @ModelAttribute("resetPasswordDto") @Valid ResetPasswordDto resetPasswordDto,
            BindingResult bindingResult,
            Model model
    ) {
        if (!bindingResult.hasErrors()
                && !resetPasswordDto.getPassword().equals(resetPasswordDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            return "reset-password";
        }

        Optional<PasswordResetToken> tokenOpt =
                passwordResetTokenService.validatePasswordResetToken(resetPasswordDto.getToken());
        if (tokenOpt.isEmpty()) {
            model.addAttribute("tokenError", "Invalid or expired token");
            return "reset-password";
        }

        PasswordResetToken token = tokenOpt.get();
        User user = token.getUser();
        passwordResetTokenService.updatePassword(user, resetPasswordDto.getPassword());
        passwordResetTokenService.markTokenAsUsed(token);

        model.addAttribute("resetSuccess", true);
        return "reset-password";
    }
}
