package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationTemplateDto;
import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;

    public List<NotificationTemplateDto> getAllTemplates() {
        return templateRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationTemplateDto> getTemplatesByType(NotificationTemplate.NotificationType type) {
        return templateRepository.findByTypeAndIsActiveTrue(type)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<NotificationTemplateDto> getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<NotificationTemplateDto> getTemplateByName(String name) {
        return templateRepository.findByNameAndIsActiveTrue(name)
                .map(this::convertToDto);
    }

    public NotificationTemplateDto createTemplate(NotificationTemplateDto templateDto) {
        if (templateRepository.existsByName(templateDto.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDto.getName() + "' already exists");
        }

        NotificationTemplate template = convertToEntity(templateDto);
        template = templateRepository.save(template);
        return convertToDto(template);
    }

    public NotificationTemplateDto updateTemplate(Long id, NotificationTemplateDto templateDto) {
        NotificationTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing template
        if (!existingTemplate.getName().equals(templateDto.getName()) &&
                templateRepository.existsByName(templateDto.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDto.getName() + "' already exists");
        }

        existingTemplate.setName(templateDto.getName());
        existingTemplate.setType(templateDto.getType());
        existingTemplate.setSubject(templateDto.getSubject());
        existingTemplate.setContent(templateDto.getContent());
        existingTemplate.setVariables(templateDto.getVariables());
        existingTemplate.setIsActive(templateDto.getIsActive());

        existingTemplate = templateRepository.save(existingTemplate);
        return convertToDto(existingTemplate);
    }

    public void deleteTemplate(Long id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));
        template.setIsActive(false);
        templateRepository.save(template);
    }

    public String processTemplate(String templateName, Map<String, Object> variables) {
        NotificationTemplate template = templateRepository.findByNameAndIsActiveTrue(templateName)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateName));

        return processTemplateContent(template.getContent(), variables);
    }

    public String processTemplateContent(String content, Map<String, Object> variables) {
        String processedContent = content;

        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processedContent = processedContent.replace(placeholder, value);
        }

        return processedContent;
    }

    private NotificationTemplateDto convertToDto(NotificationTemplate template) {
        return new NotificationTemplateDto(
                template.getId(),
                template.getName(),
                template.getType(),
                template.getSubject(),
                template.getContent(),
                template.getVariables(),
                template.getIsActive());
    }

    private NotificationTemplate convertToEntity(NotificationTemplateDto dto) {
        return new NotificationTemplate(
                dto.getId(),
                dto.getName(),
                dto.getType(),
                dto.getSubject(),
                dto.getContent(),
                dto.getVariables(),
                dto.getIsActive(),
                null,
                null);
    }
}