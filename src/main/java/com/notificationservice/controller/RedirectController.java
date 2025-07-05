package com.notificationservice.controller;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.aspect.RateLimitType;
import com.notificationservice.service.HelperService;
import com.notificationservice.service.UrlShortenerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/s")
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final UrlShortenerService urlShortenerService;
    private final HelperService helperService;

    @GetMapping("/{shortCode}")
    @RateLimited(type = RateLimitType.REDIRECT)
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode, HttpServletRequest request) {
        log.info("Redirecting short code: {}", shortCode);

        try {
            String ipAddress = helperService.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            String originalUrl = urlShortenerService.redirectToOriginalUrl(shortCode, ipAddress, userAgent);

            log.info("Redirecting {} to: {}", shortCode, originalUrl);
            return new RedirectView(originalUrl);

        } catch (IllegalArgumentException e) {
            log.error("Short code not found: {}", shortCode);
            // Redirect to a 404 page or error page
            RedirectView errorView = new RedirectView("/error/404");
            errorView.setStatusCode(HttpStatus.NOT_FOUND);
            return errorView;

        } catch (IllegalStateException e) {
            log.error("URL has expired: {}", shortCode);
            // Redirect to an expired page
            RedirectView expiredView = new RedirectView("/error/expired");
            expiredView.setStatusCode(HttpStatus.GONE);
            return expiredView;
        }
    }


}