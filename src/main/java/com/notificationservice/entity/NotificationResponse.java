package com.notificationservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private NotificationRequest request;

    @Column(name = "provider_response_id")
    private String providerResponseId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "response_data", columnDefinition = "jsonb")
    private JsonNode responseData;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}