package com.notificationservice.repository;

import com.notificationservice.entity.NotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {

    Page<NotificationRequest> findByStatus(NotificationRequest.NotificationStatus status, Pageable pageable);

    List<NotificationRequest> findByStatusAndCreatedAtBefore(NotificationRequest.NotificationStatus status,
            LocalDateTime before);

    @Query("SELECT nr FROM NotificationRequest nr WHERE nr.recipient = :recipient ORDER BY nr.createdAt DESC")
    List<NotificationRequest> findByRecipientOrderByCreatedAtDesc(@Param("recipient") String recipient);

    @Query("SELECT nr FROM NotificationRequest nr WHERE nr.createdAt >= :since ORDER BY nr.createdAt DESC")
    List<NotificationRequest> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("since") LocalDateTime since);
}