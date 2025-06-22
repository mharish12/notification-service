package com.notificationservice.repository;

import com.notificationservice.entity.EmailSender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailSenderRepository extends JpaRepository<EmailSender, Long> {

    Optional<EmailSender> findByNameAndIsActiveTrue(String name);

    List<EmailSender> findByIsActiveTrue();

    boolean existsByName(String name);
}