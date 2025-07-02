package com.notificationservice.mapper;

import com.notificationservice.dto.NotificationRuleDto;
import com.notificationservice.entity.NotificationRule;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationRuleMapper {
    public static NotificationRuleDto toDto(NotificationRule rule) {
        if (rule == null)
            return null;
        return NotificationRuleDto.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .userId(rule.getUserId())
                .ruleType(rule.getRuleType())
                .notificationType(rule.getNotificationType())
                .isActive(rule.getIsActive())
                .priority(rule.getPriority())
                .daysOfWeek(rule.getDaysOfWeek())
                .startTime(rule.getStartTime())
                .endTime(rule.getEndTime())
                .timezone(rule.getTimezone())
                .maxNotificationsPerDay(rule.getMaxNotificationsPerDay())
                .minIntervalMinutes(rule.getMinIntervalMinutes())
                .conditions(rule.getConditions())
                .variables(rule.getVariables())
                .actionType(rule.getActionType())
                .actionConfig(rule.getActionConfig())
                .templateId(rule.getTemplate() != null ? rule.getTemplate().getId() : null)
                .templateName(rule.getTemplate() != null ? rule.getTemplate().getName() : null)
                .build();
    }

    public static NotificationRule toEntity(NotificationRuleDto dto) {
        if (dto == null)
            return null;
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
        // Template is set in service if needed
        return rule;
    }

    public static List<NotificationRuleDto> toDtoList(List<NotificationRule> rules) {
        return rules == null ? null : rules.stream().map(NotificationRuleMapper::toDto).collect(Collectors.toList());
    }

    public static List<NotificationRule> toEntityList(List<NotificationRuleDto> dtos) {
        return dtos == null ? null : dtos.stream().map(NotificationRuleMapper::toEntity).collect(Collectors.toList());
    }

    public static void updateEntityFromDto(NotificationRule entity, NotificationRuleDto dto) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setUserId(dto.getUserId());
        entity.setRuleType(dto.getRuleType());
        entity.setNotificationType(dto.getNotificationType());
        entity.setIsActive(dto.getIsActive());
        entity.setPriority(dto.getPriority());
        entity.setDaysOfWeek(dto.getDaysOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setTimezone(dto.getTimezone());
        entity.setMaxNotificationsPerDay(dto.getMaxNotificationsPerDay());
        entity.setMinIntervalMinutes(dto.getMinIntervalMinutes());
        entity.setConditions(dto.getConditions());
        entity.setVariables(dto.getVariables());
        entity.setActionType(dto.getActionType());
        entity.setActionConfig(dto.getActionConfig());
        // Template is set in service if needed
    }
}