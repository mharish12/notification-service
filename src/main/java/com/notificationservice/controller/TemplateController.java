package com.notificationservice.controller;

import com.notificationservice.dto.NotificationTemplateDto;
import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@Slf4j
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<NotificationTemplateDto>> getAllTemplates() {
        List<NotificationTemplateDto> templates = templateService.getAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationTemplateDto>> getTemplatesByType(
            @PathVariable NotificationTemplate.NotificationType type) {
        List<NotificationTemplateDto> templates = templateService.getTemplatesByType(type);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplateDto> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<NotificationTemplateDto> getTemplateByName(@PathVariable String name) {
        return templateService.getTemplateByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<NotificationTemplateDto> createTemplate(
            @Valid @RequestBody NotificationTemplateDto templateDto) {
        try {
            NotificationTemplateDto created = templateService.createTemplate(templateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotificationTemplateDto> updateTemplate(@PathVariable Long id,
            @Valid @RequestBody NotificationTemplateDto templateDto) {
        try {
            NotificationTemplateDto updated = templateService.updateTemplate(id, templateDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            templateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{name}/process")
    public ResponseEntity<String> processTemplate(@PathVariable String name,
            @RequestBody Map<String, Object> variables) {
        try {
            String processed = templateService.processTemplate(name, variables);
            return ResponseEntity.ok(processed);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}