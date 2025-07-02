package com.notificationservice.repository;

import com.notificationservice.entity.RFQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RFQRepository extends JpaRepository<RFQ, Long> {
    Optional<RFQ> findByReferenceNumber(String referenceNumber);
}