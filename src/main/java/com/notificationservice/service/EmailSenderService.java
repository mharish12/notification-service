package com.notificationservice.service;

import com.notificationservice.dto.EmailSenderDto;
import com.notificationservice.entity.EmailSender;
import com.notificationservice.repository.EmailSenderRepository;
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
public class EmailSenderService {

    private final EmailSenderRepository emailSenderRepository;

    public List<EmailSenderDto> getAllSenders() {
        return emailSenderRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<EmailSenderDto> getSenderById(Long id) {
        return emailSenderRepository.findById(id)
                .map(this::convertToDto);
    }

    public Optional<EmailSenderDto> getSenderByName(String name) {
        return emailSenderRepository.findByNameAndIsActiveTrue(name)
                .map(this::convertToDto);
    }

    public EmailSenderDto createSender(EmailSenderDto senderDto) {
        if (emailSenderRepository.existsByName(senderDto.getName())) {
            throw new IllegalArgumentException("Email sender with name '" + senderDto.getName() + "' already exists");
        }

        EmailSender sender = convertToEntity(senderDto);
        sender = emailSenderRepository.save(sender);
        return convertToDto(sender);
    }

    public EmailSenderDto updateSender(Long id, EmailSenderDto senderDto) {
        EmailSender existingSender = emailSenderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email sender not found with id: " + id));

        if (!existingSender.getName().equals(senderDto.getName()) &&
                emailSenderRepository.existsByName(senderDto.getName())) {
            throw new IllegalArgumentException("Email sender with name '" + senderDto.getName() + "' already exists");
        }

        existingSender.setName(senderDto.getName());
        existingSender.setHost(senderDto.getHost());
        existingSender.setPort(senderDto.getPort());
        existingSender.setUsername(senderDto.getUsername());
        existingSender.setPassword(senderDto.getPassword());
        existingSender.setProperties(senderDto.getProperties());
        existingSender.setIsActive(senderDto.getIsActive());

        existingSender = emailSenderRepository.save(existingSender);
        return convertToDto(existingSender);
    }

    public void deleteSender(Long id) {
        EmailSender sender = emailSenderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email sender not found with id: " + id));
        sender.setIsActive(false);
        emailSenderRepository.save(sender);
    }

    private EmailSenderDto convertToDto(EmailSender sender) {
        EmailSenderDto dto = new EmailSenderDto();
        dto.setId(sender.getId());
        dto.setName(sender.getName());
        dto.setHost(sender.getHost());
        dto.setPort(sender.getPort());
        dto.setUsername(sender.getUsername());
        dto.setPassword(sender.getPassword());
        dto.setProperties(sender.getProperties());
        dto.setIsActive(sender.getIsActive());

        // Set audit fields
        dto.setCreatedAt(sender.getCreatedAt());
        dto.setModifiedAt(sender.getModifiedAt());
        dto.setCreatedBy(sender.getCreatedBy());
        dto.setModifiedBy(sender.getModifiedBy());

        return dto;
    }

    private EmailSender convertToEntity(EmailSenderDto dto) {
        EmailSender sender = new EmailSender();
        sender.setId(dto.getId());
        sender.setName(dto.getName());
        sender.setHost(dto.getHost());
        sender.setPort(dto.getPort());
        sender.setUsername(dto.getUsername());
        sender.setPassword(dto.getPassword());
        sender.setProperties(dto.getProperties());
        sender.setIsActive(dto.getIsActive());

        return sender;
    }
}