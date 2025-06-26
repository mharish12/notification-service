package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.entity.NotificationRequest;
import com.notificationservice.entity.NotificationResponse;
import com.notificationservice.repository.NotificationRequestRepository;
import com.notificationservice.repository.NotificationResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MobileMessageService {

    private final NotificationRequestRepository requestRepository;
    private final NotificationResponseRepository responseRepository;
    private final TemplateService templateService;

    // Store connected clients by network/room
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> networkClients = new ConcurrentHashMap<>();

    /**
     * Broadcast message to all clients in the same network
     */
    public NotificationRequestDto broadcastMessage(String networkId, String content, Map<String, Object> variables) {
        // Process template if variables are provided
        String processedContent = content;
        if (variables != null && !variables.isEmpty()) {
            processedContent = templateService.processTemplateContent(content, variables);
        }

        // Create notification request
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("NETWORK:" + networkId);
        request.setContent(processedContent);
        request.setVariables(convertToJsonNode(variables));
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);

        request = requestRepository.save(request);

        try {
            // Get all clients in the network
            List<String> clients = getClientsInNetwork(networkId);

            if (clients.isEmpty()) {
                log.warn("No clients found in network: {}", networkId);
                request.setStatus(NotificationRequest.NotificationStatus.FAILED);
                request.setErrorMessage("No clients found in network");
                request = requestRepository.save(request);
                return convertToDto(request);
            }

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.SENT);
            request.setSentAt(LocalDateTime.now());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("BROADCAST_SENT");
            response.setProviderResponseId("broadcast-" + System.currentTimeMillis());
            response.setResponseData(convertToJsonNode(Map.of(
                    "networkId", networkId,
                    "recipients", clients.size(),
                    "clients", clients,
                    "timestamp", LocalDateTime.now().toString())));
            responseRepository.save(response);

            log.info("Broadcast message sent successfully to network: {} with {} recipients", networkId,
                    clients.size());

        } catch (Exception e) {
            log.error("Failed to broadcast message to network: {}", networkId, e);

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.FAILED);
            request.setErrorMessage(e.getMessage());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("BROADCAST_FAILED");
            response.setResponseData(convertToJsonNode(Map.of("error", e.getMessage())));
            responseRepository.save(response);
        }

        return convertToDto(request);
    }

    /**
     * Send message to specific client in a network
     */
    public NotificationRequestDto sendToClient(String networkId, String clientId, String content,
            Map<String, Object> variables) {
        // Process template if variables are provided
        String processedContent = content;
        if (variables != null && !variables.isEmpty()) {
            processedContent = templateService.processTemplateContent(content, variables);
        }

        // Create notification request
        NotificationRequest request = new NotificationRequest();
        request.setRecipient("CLIENT:" + networkId + ":" + clientId);
        request.setContent(processedContent);
        request.setVariables(convertToJsonNode(variables));
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);

        request = requestRepository.save(request);

        try {
            // Check if client exists in the network
            if (!isClientInNetwork(networkId, clientId)) {
                throw new IllegalArgumentException("Client " + clientId + " not found in network " + networkId);
            }

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.SENT);
            request.setSentAt(LocalDateTime.now());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("CLIENT_SENT");
            response.setProviderResponseId("client-" + System.currentTimeMillis());
            response.setResponseData(convertToJsonNode(Map.of(
                    "networkId", networkId,
                    "clientId", clientId,
                    "timestamp", LocalDateTime.now().toString())));
            responseRepository.save(response);

            log.info("Message sent successfully to client: {} in network: {}", clientId, networkId);

        } catch (Exception e) {
            log.error("Failed to send message to client: {} in network: {}", clientId, networkId, e);

            // Update request status
            request.setStatus(NotificationRequest.NotificationStatus.FAILED);
            request.setErrorMessage(e.getMessage());
            request = requestRepository.save(request);

            // Create response record
            NotificationResponse response = new NotificationResponse();
            response.setRequest(request);
            response.setStatus("CLIENT_FAILED");
            response.setResponseData(convertToJsonNode(Map.of("error", e.getMessage())));
            responseRepository.save(response);
        }

        return convertToDto(request);
    }

    /**
     * Register a client to a network
     */
    public void registerClient(String networkId, String clientId, String clientInfo) {
        networkClients.computeIfAbsent(networkId, k -> new ConcurrentHashMap<>())
                .put(clientId, clientInfo);
        log.info("Client {} registered to network {}", clientId, networkId);
    }

    /**
     * Unregister a client from a network
     */
    public void unregisterClient(String networkId, String clientId) {
        ConcurrentHashMap<String, String> clients = networkClients.get(networkId);
        if (clients != null) {
            clients.remove(clientId);
            if (clients.isEmpty()) {
                networkClients.remove(networkId);
            }
        }
        log.info("Client {} unregistered from network {}", clientId, networkId);
    }

    /**
     * Get all clients in a network
     */
    public List<String> getClientsInNetwork(String networkId) {
        ConcurrentHashMap<String, String> clients = networkClients.get(networkId);
        return clients != null ? new ArrayList<>(clients.keySet()) : List.of();
    }

    /**
     * Check if a client is in a network
     */
    public boolean isClientInNetwork(String networkId, String clientId) {
        ConcurrentHashMap<String, String> clients = networkClients.get(networkId);
        return clients != null && clients.containsKey(clientId);
    }

    /**
     * Get all networks
     */
    public List<String> getAllNetworks() {
        return new ArrayList<>(networkClients.keySet());
    }

    /**
     * Get network statistics
     */
    public Map<String, Object> getNetworkStats(String networkId) {
        ConcurrentHashMap<String, String> clients = networkClients.get(networkId);
        return Map.of(
                "networkId", networkId,
                "clientCount", clients != null ? clients.size() : 0,
                "clients", clients != null ? clients.keySet() : List.of());
    }

    private JsonNode convertToJsonNode(Map<String, Object> map) {
        if (map == null)
            return null;
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(map);
        } catch (Exception e) {
            log.warn("Failed to convert map to JsonNode", e);
            return null;
        }
    }

    private NotificationRequestDto convertToDto(NotificationRequest request) {
        NotificationRequestDto dto = new NotificationRequestDto();
        dto.setId(request.getId());
        dto.setTemplateId(request.getTemplate() != null ? request.getTemplate().getId() : null);
        dto.setTemplateName(request.getTemplate() != null ? request.getTemplate().getName() : null);
        dto.setSenderId(request.getSender() != null ? request.getSender().getId() : null);
        dto.setSenderName(request.getSender() != null ? request.getSender().getName() : null);
        dto.setRecipient(request.getRecipient());
        dto.setSubject(request.getSubject());
        dto.setContent(request.getContent());
        dto.setVariables(request.getVariables());
        dto.setStatus(request.getStatus().name());
        dto.setErrorMessage(request.getErrorMessage());

        // Set audit fields
        dto.setCreatedAt(request.getCreatedAt());
        dto.setModifiedAt(request.getModifiedAt());
        dto.setCreatedBy(request.getCreatedBy());
        dto.setModifiedBy(request.getModifiedBy());

        return dto;
    }
}