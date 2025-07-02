package com.notificationservice.service;

import com.notificationservice.dto.EmailSenderDto;
import com.notificationservice.entity.EmailSender;
import com.notificationservice.repository.EmailSenderRepository;
import com.notificationservice.mapper.EmailSenderMapper;
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
        return EmailSenderMapper.toDtoList(emailSenderRepository.findByIsActiveTrue());
    }

    public Optional<EmailSenderDto> getSenderById(Long id) {
        return emailSenderRepository.findById(id)
                .map(EmailSenderMapper::toDto);
    }

    public Optional<EmailSenderDto> getSenderByName(String name) {
        return emailSenderRepository.findByNameAndIsActiveTrue(name)
                .map(EmailSenderMapper::toDto);
    }

    public EmailSenderDto createSender(EmailSenderDto senderDto) {
        if (emailSenderRepository.existsByName(senderDto.getName())) {
            throw new IllegalArgumentException("Email sender with name '" + senderDto.getName() + "' already exists");
        }

        EmailSender sender = EmailSenderMapper.toEntity(senderDto);
        sender = emailSenderRepository.save(sender);
        return EmailSenderMapper.toDto(sender);
    }

    public EmailSenderDto updateSender(Long id, EmailSenderDto senderDto) {
        EmailSender existingSender = emailSenderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Email sender not found with id: " + id));

        if (!existingSender.getName().equals(senderDto.getName()) &&
                emailSenderRepository.existsByName(senderDto.getName())) {
            throw new IllegalArgumentException("Email sender with name '" + senderDto.getName() + "' already exists");
        }

        EmailSenderMapper.updateEntityFromDto(existingSender, senderDto);
        existingSender = emailSenderRepository.save(existingSender);
        return EmailSenderMapper.toDto(existingSender);
    }

    public void deleteSender(Long id) {
        emailSenderRepository.deleteById(id);
    }
}