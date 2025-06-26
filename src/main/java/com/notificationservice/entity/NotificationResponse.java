package com.notificationservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.converter.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "notification_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationResponse extends BaseAuditableEntity {

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

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "response_data", columnDefinition = "text")
    private JsonNode responseData;
}