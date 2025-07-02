package com.notificationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationservice.dto.NotificationRequestDto;
import com.notificationservice.dto.NotificationRuleDto;
import com.notificationservice.entity.NotificationRule;
import com.notificationservice.repository.NotificationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RuleEngineService {

    private final NotificationRuleRepository ruleRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;
    private final MobileMessageService mobileMessageService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    // Cache for user notification counts (in-memory for now, could be moved to
    // Redis)
    private final Map<String, UserNotificationStats> userStats = new ConcurrentHashMap<>();

    /**
     * Evaluate rules for a user and determine if notification should be sent
     */
    public RuleEvaluationResult evaluateRules(String userId, String content, Map<String, Object> variables) {
        log.info("Evaluating rules for user: {}", userId);

        List<NotificationRule> activeRules = ruleRepository.findByUserIdAndIsActiveTrueOrderByPriorityDesc(userId);
        if (activeRules.isEmpty()) {
            log.info("No active rules found for user: {}", userId);
            return RuleEvaluationResult.allow();
        }

        RuleEvaluationResult result = new RuleEvaluationResult();

        for (NotificationRule rule : activeRules) {
            boolean shouldApply = evaluateRule(rule, content, variables);

            if (shouldApply) {
                result.addAppliedRule(convertToDto(rule));

                // Check if rule action is to block
                if ("BLOCK".equals(rule.getActionType())) {
                    result.setBlocked(true);
                    result.setBlockReason("Rule '" + rule.getName() + "' blocked the notification");
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Evaluate a single rule
     */
    private boolean evaluateRule(NotificationRule rule, String content, Map<String, Object> variables) {
        try {
            switch (rule.getRuleType()) {
                case TIME_BASED:
                    return evaluateTimeBasedRule(rule);
                case FREQUENCY_BASED:
                    return evaluateFrequencyBasedRule(rule);
                case CONTENT_BASED:
                    return evaluateContentBasedRule(rule, content, variables);
                case COMPOSITE:
                    return evaluateCompositeRule(rule, content, variables);
                default:
                    log.warn("Unknown rule type: {}", rule.getRuleType());
                    return false;
            }
        } catch (Exception e) {
            log.error("Error evaluating rule: {}", rule.getName(), e);
            return false;
        }
    }

    /**
     * Evaluate time-based rule
     */
    private boolean evaluateTimeBasedRule(NotificationRule rule) {
        ZoneId zoneId = ZoneId.of(rule.getTimezone());
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        DayOfWeek currentDay = now.getDayOfWeek();
        LocalTime currentTime = now.toLocalTime();

        // Check if current day is in allowed days
        if (rule.getDaysOfWeek() != null && !rule.getDaysOfWeek().isEmpty()) {
            if (!rule.getDaysOfWeek().contains(currentDay)) {
                return false;
            }
        }

        // Check if current time is within allowed time range
        if (rule.getStartTime() != null && rule.getEndTime() != null) {
            if (currentTime.isBefore(rule.getStartTime()) || currentTime.isAfter(rule.getEndTime())) {
                return false;
            }
        } else if (rule.getStartTime() != null) {
            if (currentTime.isBefore(rule.getStartTime())) {
                return false;
            }
        } else if (rule.getEndTime() != null) {
            if (currentTime.isAfter(rule.getEndTime())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Evaluate frequency-based rule
     */
    private boolean evaluateFrequencyBasedRule(NotificationRule rule) {
        UserNotificationStats stats = getUserStats(rule.getUserId());

        // Check daily limit
        if (rule.getMaxNotificationsPerDay() != null) {
            if (stats.getDailyCount() >= rule.getMaxNotificationsPerDay()) {
                log.info("Daily notification limit reached for user: {}", rule.getUserId());
                return false;
            }
        }

        // Check minimum interval
        if (rule.getMinIntervalMinutes() != null) {
            LocalDateTime lastNotification = stats.getLastNotificationTime();
            if (lastNotification != null) {
                LocalDateTime minNextTime = lastNotification.plusMinutes(rule.getMinIntervalMinutes());
                if (LocalDateTime.now().isBefore(minNextTime)) {
                    log.info("Minimum interval not met for user: {}", rule.getUserId());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Evaluate content-based rule
     */
    private boolean evaluateContentBasedRule(NotificationRule rule, String content, Map<String, Object> variables) {
        if (rule.getConditions() == null) {
            return true;
        }

        try {
            JsonNode conditions = rule.getConditions();

            // Check content length
            if (conditions.has("maxContentLength")) {
                int maxLength = conditions.get("maxContentLength").asInt();
                if (content.length() > maxLength) {
                    return false;
                }
            }

            // Check content keywords
            if (conditions.has("blockedKeywords")) {
                JsonNode blockedKeywords = conditions.get("blockedKeywords");
                if (blockedKeywords.isArray()) {
                    for (JsonNode keyword : blockedKeywords) {
                        if (content.toLowerCase().contains(keyword.asText().toLowerCase())) {
                            return false;
                        }
                    }
                }
            }

            // Check required keywords
            if (conditions.has("requiredKeywords")) {
                JsonNode requiredKeywords = conditions.get("requiredKeywords");
                if (requiredKeywords.isArray()) {
                    boolean hasRequiredKeyword = false;
                    for (JsonNode keyword : requiredKeywords) {
                        if (content.toLowerCase().contains(keyword.asText().toLowerCase())) {
                            hasRequiredKeyword = true;
                            break;
                        }
                    }
                    if (!hasRequiredKeyword) {
                        return false;
                    }
                }
            }

            // Check variable conditions
            if (conditions.has("variableConditions") && variables != null) {
                JsonNode varConditions = conditions.get("variableConditions");
                if (!evaluateVariableConditions(varConditions, variables)) {
                    return false;
                }
            }

        } catch (Exception e) {
            log.error("Error evaluating content-based rule: {}", rule.getName(), e);
            return false;
        }

        return true;
    }

    /**
     * Evaluate composite rule (combination of multiple rule types)
     */
    private boolean evaluateCompositeRule(NotificationRule rule, String content, Map<String, Object> variables) {
        if (rule.getConditions() == null) {
            return true;
        }

        try {
            JsonNode conditions = rule.getConditions();

            // Check if all conditions must be met (AND) or any condition (OR)
            boolean requireAll = conditions.has("requireAll") ? conditions.get("requireAll").asBoolean() : true;

            boolean timeBasedResult = true;
            boolean frequencyBasedResult = true;
            boolean contentBasedResult = true;

            // Evaluate time-based conditions
            if (conditions.has("timeBased")) {
                timeBasedResult = evaluateTimeBasedRule(rule);
            }

            // Evaluate frequency-based conditions
            if (conditions.has("frequencyBased")) {
                frequencyBasedResult = evaluateFrequencyBasedRule(rule);
            }

            // Evaluate content-based conditions
            if (conditions.has("contentBased")) {
                contentBasedResult = evaluateContentBasedRule(rule, content, variables);
            }

            if (requireAll) {
                return timeBasedResult && frequencyBasedResult && contentBasedResult;
            } else {
                return timeBasedResult || frequencyBasedResult || contentBasedResult;
            }

        } catch (Exception e) {
            log.error("Error evaluating composite rule: {}", rule.getName(), e);
            return false;
        }
    }

    /**
     * Evaluate variable conditions
     */
    private boolean evaluateVariableConditions(JsonNode varConditions, Map<String, Object> variables) {
        for (Iterator<String> it = varConditions.fieldNames(); it.hasNext();) {
            String varName = it.next();
            JsonNode condition = varConditions.get(varName);

            Object varValue = variables.get(varName);
            if (varValue == null) {
                return false;
            }

            if (condition.has("equals")) {
                if (!varValue.toString().equals(condition.get("equals").asText())) {
                    return false;
                }
            }

            if (condition.has("notEquals")) {
                if (varValue.toString().equals(condition.get("notEquals").asText())) {
                    return false;
                }
            }

            if (condition.has("contains")) {
                if (!varValue.toString().contains(condition.get("contains").asText())) {
                    return false;
                }
            }

            if (condition.has("minLength")) {
                if (varValue.toString().length() < condition.get("minLength").asInt()) {
                    return false;
                }
            }

            if (condition.has("maxLength")) {
                if (varValue.toString().length() > condition.get("maxLength").asInt()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Execute rule actions
     */
    public NotificationRequestDto executeRuleAction(NotificationRule rule, String content,
            Map<String, Object> variables) {
        try {
            switch (rule.getNotificationType()) {
                case EMAIL:
                    return executeEmailAction(rule, content, variables);
                case WHATSAPP:
                    return executeWhatsAppAction(rule, content, variables);
                case MOBILE_BROADCAST:
                    return executeMobileBroadcastAction(rule, content, variables);
                default:
                    log.warn("Unsupported notification type: {}", rule.getNotificationType());
                    return null;
            }
        } catch (Exception e) {
            log.error("Error executing rule action: {}", rule.getName(), e);
            return null;
        }
    }

    private NotificationRequestDto executeEmailAction(NotificationRule rule, String content,
            Map<String, Object> variables) {
        String recipient = extractRecipient(rule, variables);
        String subject = extractSubject(rule, variables);
        String senderName = extractSenderName(rule, variables);

        if (rule.getTemplate() != null) {
            return emailService.sendEmailWithTemplate(
                    senderName,
                    rule.getTemplate().getName(),
                    recipient,
                    variables);
        } else {
            return emailService.sendEmail(
                    senderName,
                    recipient,
                    subject,
                    content,
                    variables);
        }
    }

    private NotificationRequestDto executeWhatsAppAction(NotificationRule rule, String content,
            Map<String, Object> variables) {
        String recipient = extractRecipient(rule, variables);

        if (rule.getTemplate() != null) {
            return whatsAppService.sendWhatsAppWithTemplate(
                    rule.getTemplate().getName(),
                    recipient,
                    variables);
        } else {
            return whatsAppService.sendWhatsAppMessage(
                    recipient,
                    content,
                    variables);
        }
    }

    private NotificationRequestDto executeMobileBroadcastAction(NotificationRule rule, String content,
            Map<String, Object> variables) {
        String networkId = extractNetworkId(rule, variables);

        return mobileMessageService.broadcastMessage(
                networkId,
                content,
                variables);
    }

    private String extractRecipient(NotificationRule rule, Map<String, Object> variables) {
        if (rule.getActionConfig() != null && rule.getActionConfig().has("recipient")) {
            return rule.getActionConfig().get("recipient").asText();
        }

        if (variables != null && variables.containsKey("recipient")) {
            return variables.get("recipient").toString();
        }

        return rule.getUserId(); // fallback to user ID
    }

    private String extractSubject(NotificationRule rule, Map<String, Object> variables) {
        if (rule.getActionConfig() != null && rule.getActionConfig().has("subject")) {
            return rule.getActionConfig().get("subject").asText();
        }

        if (variables != null && variables.containsKey("subject")) {
            return variables.get("subject").toString();
        }

        return "Notification"; // default subject
    }

    private String extractSenderName(NotificationRule rule, Map<String, Object> variables) {
        if (rule.getActionConfig() != null && rule.getActionConfig().has("senderName")) {
            return rule.getActionConfig().get("senderName").asText();
        }

        if (variables != null && variables.containsKey("senderName")) {
            return variables.get("senderName").toString();
        }

        return "Notification Service"; // default sender name
    }

    private String extractNetworkId(NotificationRule rule, Map<String, Object> variables) {
        if (rule.getActionConfig() != null && rule.getActionConfig().has("networkId")) {
            return rule.getActionConfig().get("networkId").asText();
        }

        if (variables != null && variables.containsKey("networkId")) {
            return variables.get("networkId").toString();
        }

        return "default"; // default network
    }

    /**
     * Update user notification statistics
     */
    public void updateUserStats(String userId) {
        UserNotificationStats stats = getUserStats(userId);
        stats.incrementDailyCount();
        stats.setLastNotificationTime(LocalDateTime.now());
    }

    private UserNotificationStats getUserStats(String userId) {
        return userStats.computeIfAbsent(userId, k -> new UserNotificationStats());
    }

    private NotificationRuleDto convertToDto(NotificationRule rule) {
        NotificationRuleDto dto = new NotificationRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setUserId(rule.getUserId());
        dto.setTemplateId(rule.getTemplate() != null ? rule.getTemplate().getId() : null);
        dto.setTemplateName(rule.getTemplate() != null ? rule.getTemplate().getName() : null);
        dto.setRuleType(rule.getRuleType());
        dto.setNotificationType(rule.getNotificationType());
        dto.setIsActive(rule.getIsActive());
        dto.setPriority(rule.getPriority());
        dto.setDaysOfWeek(rule.getDaysOfWeek());
        dto.setStartTime(rule.getStartTime());
        dto.setEndTime(rule.getEndTime());
        dto.setTimezone(rule.getTimezone());
        dto.setMaxNotificationsPerDay(rule.getMaxNotificationsPerDay());
        dto.setMinIntervalMinutes(rule.getMinIntervalMinutes());
        dto.setConditions(rule.getConditions());
        dto.setVariables(rule.getVariables());
        dto.setActionType(rule.getActionType());
        dto.setActionConfig(rule.getActionConfig());

        // Set audit fields
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setModifiedAt(rule.getModifiedAt());
        dto.setCreatedBy(rule.getCreatedBy());
        dto.setModifiedBy(rule.getModifiedBy());

        return dto;
    }

    /**
     * Result of rule evaluation
     */
    public static class RuleEvaluationResult {
        private boolean blocked = false;
        private String blockReason;
        private List<NotificationRuleDto> appliedRules = new ArrayList<>();

        public static RuleEvaluationResult allow() {
            return new RuleEvaluationResult();
        }

        public static RuleEvaluationResult block(String reason) {
            RuleEvaluationResult result = new RuleEvaluationResult();
            result.blocked = true;
            result.blockReason = reason;
            return result;
        }

        public void addAppliedRule(NotificationRuleDto rule) {
            appliedRules.add(rule);
        }

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

    /**
     * User notification statistics
     */
    private static class UserNotificationStats {
        private int dailyCount = 0;
        private LocalDateTime lastNotificationTime;
        private LocalDateTime lastResetDate = LocalDateTime.now().toLocalDate().atStartOfDay();

        public void incrementDailyCount() {
            // Reset daily count if it's a new day
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            if (lastResetDate.isBefore(today)) {
                dailyCount = 0;
                lastResetDate = today;
            }
            dailyCount++;
        }

        // Getters and setters
        public int getDailyCount() {
            return dailyCount;
        }

        public void setDailyCount(int dailyCount) {
            this.dailyCount = dailyCount;
        }

        public LocalDateTime getLastNotificationTime() {
            return lastNotificationTime;
        }

        public void setLastNotificationTime(LocalDateTime lastNotificationTime) {
            this.lastNotificationTime = lastNotificationTime;
        }
    }
}