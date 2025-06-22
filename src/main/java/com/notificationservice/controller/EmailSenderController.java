package com.notificationservice.controller;

import com.notificationservice.dto.EmailSenderDto;
import com.notificationservice.service.EmailSenderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/email-senders")
@RequiredArgsConstructor
@Slf4j
public class EmailSenderController {

    private final EmailSenderService emailSenderService;

    @GetMapping
    public ResponseEntity<List<EmailSenderDto>> getAllSenders() {
        List<EmailSenderDto> senders = emailSenderService.getAllSenders();
        return ResponseEntity.ok(senders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailSenderDto> getSenderById(@PathVariable Long id) {
        return emailSenderService.getSenderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<EmailSenderDto> getSenderByName(@PathVariable String name) {
        return emailSenderService.getSenderByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EmailSenderDto> createSender(@Valid @RequestBody EmailSenderDto senderDto) {
        try {
            EmailSenderDto created = emailSenderService.createSender(senderDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailSenderDto> updateSender(@PathVariable Long id,
            @Valid @RequestBody EmailSenderDto senderDto) {
        try {
            EmailSenderDto updated = emailSenderService.updateSender(id, senderDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSender(@PathVariable Long id) {
        try {
            emailSenderService.deleteSender(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}