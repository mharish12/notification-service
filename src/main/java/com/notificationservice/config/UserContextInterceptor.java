package com.notificationservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class UserContextInterceptor implements HandlerInterceptor {

    private final UserContextService userContextService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Get user from header (you can customize this based on your authentication
        // strategy)
        String userHeader = request.getHeader("X-User");
        String userParam = request.getParameter("user");

        if (userHeader != null && !userHeader.trim().isEmpty()) {
            userContextService.setCurrentUser(userHeader.trim());
        } else if (userParam != null && !userParam.trim().isEmpty()) {
            userContextService.setCurrentUser(userParam.trim());
        } else {
            // Default to system if no user specified
            userContextService.setCurrentUser("system");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        // Clear the user context after request completion
        userContextService.clearCurrentUser();
    }
}