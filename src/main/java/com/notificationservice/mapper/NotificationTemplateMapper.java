package com.notificationservice.mapper;

import com.notificationservice.dto.NotificationTemplateDto;
import com.notificationservice.entity.NotificationTemplate;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationTemplateMapper {
    public static NotificationTemplateDto toDto(NotificationTemplate template) {
        if (template == null)
            return null;
        return NotificationTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .type(template.getType())
                .subject(template.getSubject())
                .content(template.getContent())
                .variables(template.getVariables())
                .isActive(template.getIsActive())
                .build();
    }

    public static NotificationTemplate toEntity(NotificationTemplateDto dto) {
        if (dto == null)
            return null;
        NotificationTemplate template = new NotificationTemplate();
        template.setId(dto.getId());
        template.setName(dto.getName());
        template.setType(dto.getType());
        template.setSubject(dto.getSubject());
        template.setContent(dto.getContent());
        template.setVariables(dto.getVariables());
        template.setIsActive(dto.getIsActive());
        return template;
    }

    public static List<NotificationTemplateDto> toDtoList(List<NotificationTemplate> templates) {
        return templates == null ? null
                : templates.stream().map(NotificationTemplateMapper::toDto).collect(Collectors.toList());
    }

    public static List<NotificationTemplate> toEntityList(List<NotificationTemplateDto> dtos) {
        return dtos == null ? null
                : dtos.stream().map(NotificationTemplateMapper::toEntity).collect(Collectors.toList());
    }

    public static void updateEntityFromDto(NotificationTemplate entity, NotificationTemplateDto dto) {
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setSubject(dto.getSubject());
        entity.setContent(dto.getContent());
        entity.setVariables(dto.getVariables());
        entity.setIsActive(dto.getIsActive());
    }
}