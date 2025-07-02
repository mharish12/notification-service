package com.notificationservice.controller;

import com.notificationservice.dto.NotificationRuleDto;
import com.notificationservice.entity.NotificationRule;
import com.notificationservice.service.RuleEngineService;
import com.notificationservice.service.RuleManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
@Slf4j
public class RuleController {

    private final RuleManagementService ruleManagementService;
    private final RuleEngineService ruleEngineService;

    // Rule Management Endpoints

    /**
     * Get all rules for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationRuleDto>> getRulesByUserId(@PathVariable String userId) {
        List<NotificationRuleDto> rules = ruleManagementService.getRulesByUserId(userId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get all active rules
     */
    @GetMapping
    public ResponseEntity<List<NotificationRuleDto>> getAllActiveRules() {
        List<NotificationRuleDto> rules = ruleManagementService.getAllActiveRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rule by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationRuleDto> getRuleById(@PathVariable Long id) {
        return ruleManagementService.getRuleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get rule by name for a specific user
     */
    @GetMapping("/user/{userId}/name/{name}")
    public ResponseEntity<NotificationRuleDto> getRuleByNameAndUserId(
            @PathVariable String userId,
            @PathVariable String name) {
        return ruleManagementService.getRuleByNameAndUserId(name, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get rules by notification type for a user
     */
    @GetMapping("/user/{userId}/type/{notificationType}")
    public ResponseEntity<List<NotificationRuleDto>> getRulesByNotificationType(
            @PathVariable String userId,
            @PathVariable NotificationRule.NotificationType notificationType) {
        List<NotificationRuleDto> rules = ruleManagementService.getRulesByNotificationType(userId, notificationType);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rules by rule type for a user
     */
    @GetMapping("/user/{userId}/rule-type/{ruleType}")
    public ResponseEntity<List<NotificationRuleDto>> getRulesByRuleType(
            @PathVariable String userId,
            @PathVariable NotificationRule.RuleType ruleType) {
        List<NotificationRuleDto> rules = ruleManagementService.getRulesByRuleType(userId, ruleType);
        return ResponseEntity.ok(rules);
    }

    /**
     * Create a new rule
     */
    @PostMapping
    public ResponseEntity<NotificationRuleDto> createRule(@Valid @RequestBody NotificationRuleDto ruleDto) {
        try {
            NotificationRuleDto createdRule = ruleManagementService.createRule(ruleDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update an existing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<NotificationRuleDto> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRuleDto ruleDto) {
        try {
            NotificationRuleDto updatedRule = ruleManagementService.updateRule(id, ruleDto);
            return ResponseEntity.ok(updatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete a rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        try {
            ruleManagementService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Activate a rule
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<NotificationRuleDto> activateRule(@PathVariable Long id) {
        try {
            NotificationRuleDto activatedRule = ruleManagementService.activateRule(id);
            return ResponseEntity.ok(activatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deactivate a rule
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<NotificationRuleDto> deactivateRule(@PathVariable Long id) {
        try {
            NotificationRuleDto deactivatedRule = ruleManagementService.deactivateRule(id);
            return ResponseEntity.ok(deactivatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update rule priority
     */
    @PatchMapping("/{id}/priority")
    public ResponseEntity<NotificationRuleDto> updateRulePriority(
            @PathVariable Long id,
            @RequestBody PriorityUpdateRequest request) {
        try {
            NotificationRuleDto updatedRule = ruleManagementService.updateRulePriority(id, request.getPriority());
            return ResponseEntity.ok(updatedRule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get rules by template
     */
    @GetMapping("/template/{templateId}")
    public ResponseEntity<List<NotificationRuleDto>> getRulesByTemplate(@PathVariable Long templateId) {
        List<NotificationRuleDto> rules = ruleManagementService.getRulesByTemplate(templateId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rules by action type
     */
    @GetMapping("/action/{actionType}")
    public ResponseEntity<List<NotificationRuleDto>> getRulesByActionType(@PathVariable String actionType) {
        List<NotificationRuleDto> rules = ruleManagementService.getRulesByActionType(actionType);
        return ResponseEntity.ok(rules);
    }

    // Rule Engine Endpoints

    /**
     * Evaluate rules for a user
     */
    @PostMapping("/evaluate/{userId}")
    public ResponseEntity<RuleEvaluationResponse> evaluateRules(
            @PathVariable String userId,
            @RequestBody RuleEvaluationRequest request) {
        try {
            RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                    userId,
                    request.getContent(),
                    request.getVariables());

            RuleEvaluationResponse response = new RuleEvaluationResponse();
            response.setBlocked(result.isBlocked());
            response.setBlockReason(result.getBlockReason());
            response.setAppliedRules(result.getAppliedRules());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error evaluating rules for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Test rule evaluation with sample data
     */
    @PostMapping("/test-evaluation")
    public ResponseEntity<RuleEvaluationResponse> testRuleEvaluation(@RequestBody TestEvaluationRequest request) {
        try {
            RuleEngineService.RuleEvaluationResult result = ruleEngineService.evaluateRules(
                    request.getUserId(),
                    request.getContent(),
                    request.getVariables());

            RuleEvaluationResponse response = new RuleEvaluationResponse();
            response.setBlocked(result.isBlocked());
            response.setBlockReason(result.getBlockReason());
            response.setAppliedRules(result.getAppliedRules());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error testing rule evaluation", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Request/Response classes

    public static class PriorityUpdateRequest {
        private Integer priority;

        // Getters and setters
        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }
    }

    public static class RuleEvaluationRequest {
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

    public static class TestEvaluationRequest {
        private String userId;
        private String content;
        private Map<String, Object> variables;

        // Getters and setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

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

    public static class RuleEvaluationResponse {
        private boolean blocked;
        private String blockReason;
        private List<NotificationRuleDto> appliedRules;

        // Getters and setters
        public boolean isBlocked() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

        public String getBlockReason() {
            return blockReason;
        }

        public void setBlockReason(String blockReason) {
            this.blockReason = blockReason;
        }

        public List<NotificationRuleDto> getAppliedRules() {
            return appliedRules;
        }

        public void setAppliedRules(List<NotificationRuleDto> appliedRules) {
            this.appliedRules = appliedRules;
        }
    }
}