package com.notificationservice.repository;

import com.notificationservice.entity.NotificationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRuleRepository extends JpaRepository<NotificationRule, Long> {

    // Find active rules for a specific user
    List<NotificationRule> findByUserIdAndIsActiveTrueOrderByPriorityDesc(String userId);

    // Find active rules for a specific user and notification type
    List<NotificationRule> findByUserIdAndNotificationTypeAndIsActiveTrueOrderByPriorityDesc(
            String userId, NotificationRule.NotificationType notificationType);

    // Find active rules for a specific user and rule type
    List<NotificationRule> findByUserIdAndRuleTypeAndIsActiveTrueOrderByPriorityDesc(
            String userId, NotificationRule.RuleType ruleType);

    // Find rules by name for a user
    Optional<NotificationRule> findByNameAndUserIdAndIsActiveTrue(String name, String userId);

    // Find time-based rules that are active during specific time
    @Query("SELECT r FROM NotificationRule r WHERE r.userId = :userId " +
            "AND r.isActive = true " +
            "AND r.ruleType IN ('TIME_BASED', 'COMPOSITE') " +
            "AND (:dayOfWeek MEMBER OF r.daysOfWeek OR r.daysOfWeek IS EMPTY) " +
            "AND (r.startTime IS NULL OR r.startTime <= :currentTime) " +
            "AND (r.endTime IS NULL OR r.endTime >= :currentTime) " +
            "ORDER BY r.priority DESC")
    List<NotificationRule> findActiveTimeBasedRules(
            @Param("userId") String userId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("currentTime") LocalTime currentTime);

    // Find frequency-based rules
    @Query("SELECT r FROM NotificationRule r WHERE r.userId = :userId " +
            "AND r.isActive = true " +
            "AND r.ruleType IN ('FREQUENCY_BASED', 'COMPOSITE') " +
            "ORDER BY r.priority DESC")
    List<NotificationRule> findActiveFrequencyBasedRules(@Param("userId") String userId);

    // Find content-based rules
    @Query("SELECT r FROM NotificationRule r WHERE r.userId = :userId " +
            "AND r.isActive = true " +
            "AND r.ruleType IN ('CONTENT_BASED', 'COMPOSITE') " +
            "ORDER BY r.priority DESC")
    List<NotificationRule> findActiveContentBasedRules(@Param("userId") String userId);

    // Check if rule name exists for user
    boolean existsByNameAndUserId(String name, String userId);

    // Find all active rules
    List<NotificationRule> findByIsActiveTrueOrderByUserIdAscPriorityDesc();

    // Find rules by template
    List<NotificationRule> findByTemplateIdAndIsActiveTrue(Long templateId);

    // Find rules by action type
    List<NotificationRule> findByActionTypeAndIsActiveTrue(String actionType);
}