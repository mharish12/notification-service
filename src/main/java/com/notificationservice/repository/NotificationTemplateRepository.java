package com.notificationservice.repository;

import com.notificationservice.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByNameAndIsActiveTrue(String name);

    List<NotificationTemplate> findByTypeAndIsActiveTrue(NotificationTemplate.NotificationType type);

    List<NotificationTemplate> findByIsActiveTrue();

    @Query("SELECT t FROM NotificationTemplate t WHERE t.name = :name AND t.isActive = true")
    Optional<NotificationTemplate> findActiveTemplateByName(@Param("name") String name);

    boolean existsByName(String name);
}