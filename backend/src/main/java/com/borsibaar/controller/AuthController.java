package com.borsibaar.controller;

import com.borsibaar.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login/success")
    public void success(HttpServletResponse response, OAuth2AuthenticationToken auth) throws IOException {
        if (auth == null) {
            System.out.println("[AUTH] OAuth2AuthenticationToken is NULL. Possible session/cookie issue.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized: OAuth2 authentication required\"}");
            return;
        }
        System.out.println("[AUTH] OAuth2AuthenticationToken present. Principal: " + auth.getPrincipal());
        System.out.println("[AUTH] Authorities: " + auth.getAuthorities());
        System.out.println("[AUTH] Details: " + auth.getDetails());
        var result = authService.processOAuthLogin(auth);

        Cookie cookie = new Cookie("jwt", result.dto().token());
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS enabled with domain
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);

        String redirect = result.needsOnboarding() ? "/onboarding" : "/dashboard";
        System.out.println("[AUTH] Redirecting to: " + frontendUrl + redirect);
        response.sendRedirect(frontendUrl + redirect);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the server-side session (removes OAuth2 authentication)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear the Spring Security context
        SecurityContextHolder.clearContext();

        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // HTTPS enabled with domain
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        return ResponseEntity.ok().body(new LogoutResponse("Logged out successfully"));
    }

    private record LogoutResponse(String message) {
    }
}
