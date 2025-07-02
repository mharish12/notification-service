package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationRuleDto;
import com.notificationservice.entity.NotificationRule;
import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.NotificationRuleRepository;
import com.notificationservice.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RuleManagementService {

    private final NotificationRuleRepository ruleRepository;
    private final NotificationTemplateRepository templateRepository;

    /**
     * Get all rules for a user
     */
    public List<NotificationRuleDto> getRulesByUserId(String userId) {
        return ruleRepository.findByUserIdAndIsActiveTrueOrderByPriorityDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active rules
     */
    public List<NotificationRuleDto> getAllActiveRules() {
        return ruleRepository.findByIsActiveTrueOrderByUserIdAscPriorityDesc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get rule by ID
     */
    public Optional<NotificationRuleDto> getRuleById(Long id) {
        return ruleRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Get rule by name for a specific user
     */
    public Optional<NotificationRuleDto> getRuleByNameAndUserId(String name, String userId) {
        return ruleRepository.findByNameAndUserIdAndIsActiveTrue(name, userId)
                .map(this::convertToDto);
    }

    /**
     * Get rules by notification type for a user
     */
    public List<NotificationRuleDto> getRulesByNotificationType(String userId, NotificationRule.NotificationType type) {
        return ruleRepository.findByUserIdAndNotificationTypeAndIsActiveTrueOrderByPriorityDesc(userId, type)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get rules by rule type for a user
     */
    public List<NotificationRuleDto> getRulesByRuleType(String userId, NotificationRule.RuleType type) {
        return ruleRepository.findByUserIdAndRuleTypeAndIsActiveTrueOrderByPriorityDesc(userId, type)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create a new rule
     */
    public NotificationRuleDto createRule(NotificationRuleDto ruleDto) {
        // Validate template if provided
        if (ruleDto.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.findById(ruleDto.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Template not found with ID: " + ruleDto.getTemplateId()));
            ruleDto.setTemplateName(template.getName());
        }

        // Check if rule name already exists for this user
        if (ruleRepository.existsByNameAndUserId(ruleDto.getName(), ruleDto.getUserId())) {
            throw new IllegalArgumentException(
                    "Rule with name '" + ruleDto.getName() + "' already exists for user: " + ruleDto.getUserId());
        }

        NotificationRule rule = convertToEntity(ruleDto);
        rule = ruleRepository.save(rule);

        log.info("Created rule: {} for user: {}", rule.getName(), rule.getUserId());
        return convertToDto(rule);
    }

    /**
     * Update an existing rule
     */
    public NotificationRuleDto updateRule(Long id, NotificationRuleDto ruleDto) {
        NotificationRule existingRule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + id));

        // Validate template if provided
        if (ruleDto.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.findById(ruleDto.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Template not found with ID: " + ruleDto.getTemplateId()));
            ruleDto.setTemplateName(template.getName());
        }

        // Check if rule name already exists for this user (excluding current rule)
        if (!existingRule.getName().equals(ruleDto.getName()) &&
                ruleRepository.existsByNameAndUserId(ruleDto.getName(), ruleDto.getUserId())) {
            throw new IllegalArgumentException(
                    "Rule with name '" + ruleDto.getName() + "' already exists for user: " + ruleDto.getUserId());
        }

        // Update fields
        existingRule.setName(ruleDto.getName());
        existingRule.setDescription(ruleDto.getDescription());
        existingRule.setUserId(ruleDto.getUserId());
        existingRule.setRuleType(ruleDto.getRuleType());
        existingRule.setNotificationType(ruleDto.getNotificationType());
        existingRule.setIsActive(ruleDto.getIsActive());
        existingRule.setPriority(ruleDto.getPriority());
        existingRule.setDaysOfWeek(ruleDto.getDaysOfWeek());
        existingRule.setStartTime(ruleDto.getStartTime());
        existingRule.setEndTime(ruleDto.getEndTime());
        existingRule.setTimezone(ruleDto.getTimezone());
        existingRule.setMaxNotificationsPerDay(ruleDto.getMaxNotificationsPerDay());
        existingRule.setMinIntervalMinutes(ruleDto.getMinIntervalMinutes());
        existingRule.setConditions(ruleDto.getConditions());
        existingRule.setVariables(ruleDto.getVariables());
        existingRule.setActionType(ruleDto.getActionType());
        existingRule.setActionConfig(ruleDto.getActionConfig());

        // Update template if provided
        if (ruleDto.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.findById(ruleDto.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Template not found with ID: " + ruleDto.getTemplateId()));
            existingRule.setTemplate(template);
        } else {
            existingRule.setTemplate(null);
        }

        existingRule = ruleRepository.save(existingRule);

        log.info("Updated rule: {} for user: {}", existingRule.getName(), existingRule.getUserId());
        return convertToDto(existingRule);
    }

    /**
     * Delete a rule (soft delete by setting isActive to false)
     */
    public void deleteRule(Long id) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + id));

        rule.setIsActive(false);
        ruleRepository.save(rule);

        log.info("Deleted rule: {} for user: {}", rule.getName(), rule.getUserId());
    }

    /**
     * Activate a rule
     */
    public NotificationRuleDto activateRule(Long id) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + id));

        rule.setIsActive(true);
        rule = ruleRepository.save(rule);

        log.info("Activated rule: {} for user: {}", rule.getName(), rule.getUserId());
        return convertToDto(rule);
    }

    /**
     * Deactivate a rule
     */
    public NotificationRuleDto deactivateRule(Long id) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + id));

        rule.setIsActive(false);
        rule = ruleRepository.save(rule);

        log.info("Deactivated rule: {} for user: {}", rule.getName(), rule.getUserId());
        return convertToDto(rule);
    }

    /**
     * Update rule priority
     */
    public NotificationRuleDto updateRulePriority(Long id, Integer priority) {
        NotificationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found with ID: " + id));

        rule.setPriority(priority);
        rule = ruleRepository.save(rule);

        log.info("Updated priority for rule: {} to {}", rule.getName(), priority);
        return convertToDto(rule);
    }

    /**
     * Get rules by template
     */
    public List<NotificationRuleDto> getRulesByTemplate(Long templateId) {
        return ruleRepository.findByTemplateIdAndIsActiveTrue(templateId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get rules by action type
     */
    public List<NotificationRuleDto> getRulesByActionType(String actionType) {
        return ruleRepository.findByActionTypeAndIsActiveTrue(actionType)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NotificationRuleDto convertToDto(NotificationRule rule) {
        NotificationRuleDto dto = new NotificationRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setUserId(rule.getUserId());
        dto.setTemplateId(rule.getTemplate() != null ? rule.getTemplate().getId() : null);
        dto.setTemplateName(rule.getTemplate() != null ? rule.getTemplate().getName() : null);
        dto.setRuleType(rule.getRuleType());
        dto.setNotificationType(rule.getNotificationType());
        dto.setIsActive(rule.getIsActive());
        dto.setPriority(rule.getPriority());
        dto.setDaysOfWeek(rule.getDaysOfWeek());
        dto.setStartTime(rule.getStartTime());
        dto.setEndTime(rule.getEndTime());
        dto.setTimezone(rule.getTimezone());
        dto.setMaxNotificationsPerDay(rule.getMaxNotificationsPerDay());
        dto.setMinIntervalMinutes(rule.getMinIntervalMinutes());
        dto.setConditions(rule.getConditions());
        dto.setVariables(rule.getVariables());
        dto.setActionType(rule.getActionType());
        dto.setActionConfig(rule.getActionConfig());

        // Set audit fields
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setModifiedAt(rule.getModifiedAt());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setModifiedBy(rule.getModifiedBy());

        return dto;
    }

    private NotificationRule convertToEntity(NotificationRuleDto dto) {
        NotificationRule rule = new NotificationRule();
        rule.setId(dto.getId());
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setUserId(dto.getUserId());
        rule.setRuleType(dto.getRuleType());
        rule.setNotificationType(dto.getNotificationType());
        rule.setIsActive(dto.getIsActive());
        rule.setPriority(dto.getPriority());
        rule.setDaysOfWeek(dto.getDaysOfWeek());
        rule.setStartTime(dto.getStartTime());
        rule.setEndTime(dto.getEndTime());
        rule.setTimezone(dto.getTimezone());
        rule.setMaxNotificationsPerDay(dto.getMaxNotificationsPerDay());
        rule.setMinIntervalMinutes(dto.getMinIntervalMinutes());
        rule.setConditions(dto.getConditions());
        rule.setVariables(dto.getVariables());
        rule.setActionType(dto.getActionType());
        rule.setActionConfig(dto.getActionConfig());

        // Set template if provided
        if (dto.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.findById(dto.getTemplateId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Template not found with ID: " + dto.getTemplateId()));
            rule.setTemplate(template);
        }

        return rule;
    }
}