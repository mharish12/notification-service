package com.notificationservice.config;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
public class UserContextService {
    private final InheritableThreadLocal<String> userContext = new InheritableThreadLocal<>();

    public void setCurrentUser(String username) {
        userContext.set(username != null ? username : "system");
    }

    public String getCurrentUser() {
        String user = userContext.get();
        return user != null ? user : "system";
    }

    public void clearCurrentUser() {
        userContext.remove();
    }
}