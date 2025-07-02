package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationRuleDto;
import com.notificationservice.entity.NotificationRule;
import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.NotificationRuleRepository;
import com.notificationservice.repository.NotificationTemplateRepository;
import com.notificationservice.mapper.NotificationRuleMapper;
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
        return NotificationRuleMapper.toDtoList(ruleRepository.findByUserIdAndIsActiveTrueOrderByPriorityDesc(userId));
    }

    /**
     * Get all active rules
     */
    public List<NotificationRuleDto> getAllActiveRules() {
        return NotificationRuleMapper.toDtoList(ruleRepository.findByIsActiveTrueOrderByUserIdAscPriorityDesc());
    }

    /**
     * Get rule by ID
     */
    public Optional<NotificationRuleDto> getRuleById(Long id) {
        return ruleRepository.findById(id)
                .map(NotificationRuleMapper::toDto);
    }

    /**
     * Get rule by name for a specific user
     */
    public Optional<NotificationRuleDto> getRuleByNameAndUserId(String name, String userId) {
        return ruleRepository.findByNameAndUserIdAndIsActiveTrue(name, userId)
                .map(NotificationRuleMapper::toDto);
    }

    /**
     * Get rules by notification type for a user
     */
    public List<NotificationRuleDto> getRulesByNotificationType(String userId, NotificationRule.NotificationType type) {
        return NotificationRuleMapper.toDtoList(
                ruleRepository.findByUserIdAndNotificationTypeAndIsActiveTrueOrderByPriorityDesc(userId, type));
    }

    /**
     * Get rules by rule type for a user
     */
    public List<NotificationRuleDto> getRulesByRuleType(String userId, NotificationRule.RuleType type) {
        return NotificationRuleMapper
                .toDtoList(ruleRepository.findByUserIdAndRuleTypeAndIsActiveTrueOrderByPriorityDesc(userId, type));
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

        NotificationRule rule = NotificationRuleMapper.toEntity(ruleDto);
        // Set template if provided
        if (ruleDto.getTemplateId() != null) {
            NotificationTemplate template = templateRepository.findById(ruleDto.getTemplateId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Template not found with ID: " + ruleDto.getTemplateId()));
            rule.setTemplate(template);
        }
        rule = ruleRepository.save(rule);

        log.info("Created rule: {} for user: {}", rule.getName(), rule.getUserId());
        return NotificationRuleMapper.toDto(rule);
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
        NotificationRuleMapper.updateEntityFromDto(existingRule, ruleDto);

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
        return NotificationRuleMapper.toDto(existingRule);
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
        return NotificationRuleMapper.toDto(rule);
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
        return NotificationRuleMapper.toDto(rule);
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
        return NotificationRuleMapper.toDto(rule);
    }

    /**
     * Get rules by template
     */
    public List<NotificationRuleDto> getRulesByTemplate(Long templateId) {
        return NotificationRuleMapper.toDtoList(ruleRepository.findByTemplateIdAndIsActiveTrue(templateId));
    }

    /**
     * Get rules by action type
     */
    public List<NotificationRuleDto> getRulesByActionType(String actionType) {
        return NotificationRuleMapper.toDtoList(ruleRepository.findByActionTypeAndIsActiveTrue(actionType));
    }
}