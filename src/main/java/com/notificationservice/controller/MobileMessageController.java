package com.notificationservice.controller;

import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.service.MobileMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
@Slf4j
public class MobileMessageController {

    private final MobileMessageService mobileMessageService;

    /**
     * Broadcast message to all clients in a network
     */
    @PostMapping("/broadcast/{networkId}")
    public ResponseEntity<NotificationRequestDto> broadcastMessage(
            @PathVariable String networkId,
            @RequestBody BroadcastRequest request) {
        try {
            NotificationRequestDto result = mobileMessageService.broadcastMessage(
                    networkId,
                    request.getContent(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Send message to specific client in a network
     */
    @PostMapping("/client/{networkId}/{clientId}")
    public ResponseEntity<NotificationRequestDto> sendToClient(
            @PathVariable String networkId,
            @PathVariable String clientId,
            @RequestBody ClientMessageRequest request) {
        try {
            NotificationRequestDto result = mobileMessageService.sendToClient(
                    networkId,
                    clientId,
                    request.getContent(),
                    request.getVariables());
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Register a client to a network
     */
    @PostMapping("/register/{networkId}")
    public ResponseEntity<Map<String, Object>> registerClient(
            @PathVariable String networkId,
            @RequestBody ClientRegistrationRequest request) {
        try {
            mobileMessageService.registerClient(networkId, request.getClientId(), request.getClientInfo());
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Client registered successfully",
                    "networkId", networkId,
                    "clientId", request.getClientId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /**
     * Unregister a client from a network
     */
    @DeleteMapping("/unregister/{networkId}/{clientId}")
    public ResponseEntity<Map<String, Object>> unregisterClient(
            @PathVariable String networkId,
            @PathVariable String clientId) {
        try {
            mobileMessageService.unregisterClient(networkId, clientId);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Client unregistered successfully",
                    "networkId", networkId,
                    "clientId", clientId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /**
     * Get all clients in a network
     */
    @GetMapping("/network/{networkId}/clients")
    public ResponseEntity<Map<String, Object>> getNetworkClients(@PathVariable String networkId) {
        try {
            List<String> clients = mobileMessageService.getClientsInNetwork(networkId);
            return ResponseEntity.ok(Map.of(
                    "networkId", networkId,
                    "clientCount", clients.size(),
                    "clients", clients));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /**
     * Get all networks
     */
    @GetMapping("/networks")
    public ResponseEntity<Map<String, Object>> getAllNetworks() {
        try {
            List<String> networks = mobileMessageService.getAllNetworks();
            return ResponseEntity.ok(Map.of(
                    "networkCount", networks.size(),
                    "networks", networks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /**
     * Get network statistics
     */
    @GetMapping("/network/{networkId}/stats")
    public ResponseEntity<Map<String, Object>> getNetworkStats(@PathVariable String networkId) {
        try {
            Map<String, Object> stats = mobileMessageService.getNetworkStats(networkId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    /**
     * Check if a client is in a network
     */
    @GetMapping("/network/{networkId}/client/{clientId}")
    public ResponseEntity<Map<String, Object>> isClientInNetwork(
            @PathVariable String networkId,
            @PathVariable String clientId) {
        try {
            boolean isInNetwork = mobileMessageService.isClientInNetwork(networkId, clientId);
            return ResponseEntity.ok(Map.of(
                    "networkId", networkId,
                    "clientId", clientId,
                    "isInNetwork", isInNetwork));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }

    // Request classes
    public static class BroadcastRequest {
        private String content;
        private Map<String, Object> variables;

        // Getters and setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class ClientMessageRequest {
        private String content;
        private Map<String, Object> variables;

        // Getters and setters
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
    }

    public static class ClientRegistrationRequest {
        private String clientId;
        private String clientInfo;

        // Getters and setters
        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientInfo() {
            return clientInfo;
        }

        public void setClientInfo(String clientInfo) {
            this.clientInfo = clientInfo;
        }
    }
}
