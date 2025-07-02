package com.notificationservice.mapper;

import com.notificationservice.dto.EmailSenderDto;
import com.notificationservice.entity.EmailSender;
import java.util.List;
import java.util.stream.Collectors;

public class EmailSenderMapper {
    public static EmailSenderDto toDto(EmailSender sender) {
        if (sender == null)
            return null;
        return EmailSenderDto.builder()
                .id(sender.getId())
                .name(sender.getName())
                .host(sender.getHost())
                .port(sender.getPort())
                .username(sender.getUsername())
                .password(sender.getPassword())
                .properties(sender.getProperties())
                .isActive(sender.getIsActive())
                .build();
    }

    public static EmailSender toEntity(EmailSenderDto dto) {
        if (dto == null)
            return null;
        return EmailSender.builder()
                .id(dto.getId())
                .name(dto.getName())
                .host(dto.getHost())
                .port(dto.getPort())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .properties(dto.getProperties())
                .isActive(dto.getIsActive())
                .build();
    }

    public static List<EmailSenderDto> toDtoList(List<EmailSender> senders) {
        return senders == null ? null : senders.stream().map(EmailSenderMapper::toDto).collect(Collectors.toList());
    }

    public static List<EmailSender> toEntityList(List<EmailSenderDto> dtos) {
        return dtos == null ? null : dtos.stream().map(EmailSenderMapper::toEntity).collect(Collectors.toList());
    }

    public static void updateEntityFromDto(EmailSender entity, EmailSenderDto dto) {
        entity.setName(dto.getName());
        entity.setHost(dto.getHost());
        entity.setPort(dto.getPort());
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setProperties(dto.getProperties());
        entity.setIsActive(dto.getIsActive());
    }
}