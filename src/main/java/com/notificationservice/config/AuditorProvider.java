package com.notificationservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuditorProvider implements AuditorAware<String> {

    private final UserContextService userContextService;

    @Override
    public Optional<String> getCurrentAuditor() {
        // 1. Try to get user from current HTTP request (header or param)
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String userHeader = request.getHeader("X-User");
            String userParam = request.getParameter("user");
            if (userHeader != null && !userHeader.trim().isEmpty()) {
                return Optional.of(userHeader.trim());
            } else if (userParam != null && !userParam.trim().isEmpty()) {
                return Optional.of(userParam.trim());
            }
        }
        // 2. Fallback: use thread-local context (for async/background jobs)
        return Optional.of(userContextService.getCurrentUser());
    }
}