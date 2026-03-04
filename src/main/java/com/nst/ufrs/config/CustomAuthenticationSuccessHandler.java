package com.nst.ufrs.config;

import com.nst.ufrs.repository.UserRepository;
import com.nst.ufrs.service.impl.RunningNumberServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RunningNumberServiceImpl runningNumberService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String redirectUrl = "/dashboard";
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        Optional<com.nst.ufrs.entity.User> user1 = userRepository.findByEmail(user.getUsername());
        request.getSession().setAttribute("userID", user1.get().getId());
        request.getSession().setAttribute("name", user1.get().getName());
        request.getSession().setAttribute("email", user1.get().getEmail());

        // 🔹 Temporary hardcoded EventLocationId
        Long eventLocationId = 1L;
        request.getSession().setAttribute("eventLocationId", eventLocationId);

        // 🔹 Call RunningNumber init on login
        runningNumberService.initializeRunningNumber(eventLocationId);

        /*for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();


            if ("ROLE_CP".equals(role)) {
                redirectUrl = "/cp/dashboard";
                break;
            } else if ("ROLE_DCP".equals(role)) {
                redirectUrl = "/dcp/dashboard";
                break;
            } else if ("ROLE_ACP".equals(role)) {
                redirectUrl = "/acp/dashboard";
                break;
            } else if ("ROLE_EVENT_INCHARGE".equals(role)) {
                redirectUrl = "/event-incharge/dashboard";
                break;
            } else if ("ROLE_DATA_ENTRY".equals(role)) {
                redirectUrl = "/data-entry/dashboard";
                break;
            } else if ("ROLE_VAHAK".equals(role)) {
                redirectUrl = "/vahak/dashboard";
                break;
            } else if ("ROLE_DOCUMENT_VERIFICATION".equals(role)) {
                redirectUrl = "/document-verification/dashboard";
                break;
            }
        }*/

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }
}
