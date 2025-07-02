package com.notificationservice.service;

import com.notificationservice.dto.NotificationTemplateDto;
import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.NotificationTemplateRepository;
import com.notificationservice.mapper.NotificationTemplateMapper;
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
        return NotificationTemplateMapper.toDtoList(templateRepository.findByIsActiveTrue());
    }

    public List<NotificationTemplateDto> getTemplatesByType(NotificationTemplate.NotificationType type) {
        return NotificationTemplateMapper.toDtoList(templateRepository.findByTypeAndIsActiveTrue(type));
    }

    public Optional<NotificationTemplateDto> getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(NotificationTemplateMapper::toDto);
    }

    public Optional<NotificationTemplateDto> getTemplateByName(String name) {
        return templateRepository.findByNameAndIsActiveTrue(name)
                .map(NotificationTemplateMapper::toDto);
    }

    public NotificationTemplateDto createTemplate(NotificationTemplateDto templateDto) {
        if (templateRepository.existsByName(templateDto.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDto.getName() + "' already exists");
        }

        NotificationTemplate template = NotificationTemplateMapper.toEntity(templateDto);
        template = templateRepository.save(template);
        return NotificationTemplateMapper.toDto(template);
    }

    public NotificationTemplateDto updateTemplate(Long id, NotificationTemplateDto templateDto) {
        NotificationTemplate existingTemplate = templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + id));

        // Check if name is being changed and if it conflicts with existing template
        if (!existingTemplate.getName().equals(templateDto.getName()) &&
                templateRepository.existsByName(templateDto.getName())) {
            throw new IllegalArgumentException("Template with name '" + templateDto.getName() + "' already exists");
        }

        // Update fields
        NotificationTemplateMapper.updateEntityFromDto(existingTemplate, templateDto);

        existingTemplate = templateRepository.save(existingTemplate);
        return NotificationTemplateMapper.toDto(existingTemplate);
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
}