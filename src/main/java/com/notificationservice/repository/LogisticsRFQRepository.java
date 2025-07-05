package com.notificationservice.repository;

import com.notificationservice.entity.LogisticsRFQ;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogisticsRFQRepository extends JpaRepository<LogisticsRFQ, Long> {

    // Find by RFQ number
    Optional<LogisticsRFQ> findByRfqNumber(String rfqNumber);

    // Find by RFQ type
    List<LogisticsRFQ> findByRfqType(LogisticsRFQ.RFQType rfqType);

    // Find by RFQ type with pagination
    Page<LogisticsRFQ> findByRfqType(LogisticsRFQ.RFQType rfqType, Pageable pageable);

    // Find by status
    List<LogisticsRFQ> findByStatus(LogisticsRFQ.RFQStatus status);

    // Find by status with pagination
    Page<LogisticsRFQ> findByStatus(LogisticsRFQ.RFQStatus status, Pageable pageable);

    // Find by RFQ type and status
    List<LogisticsRFQ> findByRfqTypeAndStatus(LogisticsRFQ.RFQType rfqType, LogisticsRFQ.RFQStatus status);

    // Find by RFQ type and status with pagination
    Page<LogisticsRFQ> findByRfqTypeAndStatus(LogisticsRFQ.RFQType rfqType, LogisticsRFQ.RFQStatus status,
            Pageable pageable);

    // Find by requested by
    List<LogisticsRFQ> findByRequestedBy(String requestedBy);

    // Find by requested by with pagination
    Page<LogisticsRFQ> findByRequestedBy(String requestedBy, Pageable pageable);

    // Find by requested by and RFQ type
    List<LogisticsRFQ> findByRequestedByAndRfqType(String requestedBy, LogisticsRFQ.RFQType rfqType);

    // Find by requested by and RFQ type with pagination
    Page<LogisticsRFQ> findByRequestedByAndRfqType(String requestedBy, LogisticsRFQ.RFQType rfqType, Pageable pageable);

    // Find by cargo type
    List<LogisticsRFQ> findByCargoType(LogisticsRFQ.CargoType cargoType);

    // Find by truck type
    List<LogisticsRFQ> findByTruckType(LogisticsRFQ.TruckType truckType);

    // Find by transport mode
    List<LogisticsRFQ> findByTransportMode(LogisticsRFQ.TransportMode transportMode);

    // Find by priority
    List<LogisticsRFQ> findByPriority(LogisticsRFQ.Priority priority);

    // Find by pickup date range
    List<LogisticsRFQ> findByPickupDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by delivery date range
    List<LogisticsRFQ> findByDeliveryDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find by valid until (expired RFQs)
    List<LogisticsRFQ> findByValidUntilBefore(LocalDateTime date);

    // Find active RFQs
    List<LogisticsRFQ> findByIsActiveTrue();

    // Find active RFQs with pagination
    Page<LogisticsRFQ> findByIsActiveTrue(Pageable pageable);

    // Find by RFQ type and active status
    List<LogisticsRFQ> findByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType rfqType);

    // Find by RFQ type and active status with pagination
    Page<LogisticsRFQ> findByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType rfqType, Pageable pageable);

    // Find by status and active status
    List<LogisticsRFQ> findByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus status);

    // Find by status and active status with pagination
    Page<LogisticsRFQ> findByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus status, Pageable pageable);

    // Find by requested by and active status
    List<LogisticsRFQ> findByRequestedByAndIsActiveTrue(String requestedBy);

    // Find by requested by and active status with pagination
    Page<LogisticsRFQ> findByRequestedByAndIsActiveTrue(String requestedBy, Pageable pageable);

    // Custom query to find RFQs by multiple criteria
    @Query("SELECT lr FROM LogisticsRFQ lr WHERE " +
            "(:rfqType IS NULL OR lr.rfqType = :rfqType) AND " +
            "(:status IS NULL OR lr.status = :status) AND " +
            "(:requestedBy IS NULL OR lr.requestedBy = :requestedBy) AND " +
            "(:cargoType IS NULL OR lr.cargoType = :cargoType) AND " +
            "(:truckType IS NULL OR lr.truckType = :truckType) AND " +
            "(:transportMode IS NULL OR lr.transportMode = :transportMode) AND " +
            "(:priority IS NULL OR lr.priority = :priority) AND " +
            "lr.isActive = true")
    Page<LogisticsRFQ> findByMultipleCriteria(
            @Param("rfqType") LogisticsRFQ.RFQType rfqType,
            @Param("status") LogisticsRFQ.RFQStatus status,
            @Param("requestedBy") String requestedBy,
            @Param("cargoType") LogisticsRFQ.CargoType cargoType,
            @Param("truckType") LogisticsRFQ.TruckType truckType,
            @Param("transportMode") LogisticsRFQ.TransportMode transportMode,
            @Param("priority") LogisticsRFQ.Priority priority,
            Pageable pageable);

    // Custom query to find RFQs by location (from city/country)
    @Query("SELECT lr FROM LogisticsRFQ lr WHERE " +
            "(:fromCity IS NULL OR lr.shipFromCity = :fromCity) AND " +
            "(:fromCountry IS NULL OR lr.shipFromCountry = :fromCountry) AND " +
            "(:toCity IS NULL OR lr.shipToCity = :toCity) AND " +
            "(:toCountry IS NULL OR lr.shipToCountry = :toCountry) AND " +
            "lr.isActive = true")
    List<LogisticsRFQ> findByLocation(
            @Param("fromCity") String fromCity,
            @Param("fromCountry") String fromCountry,
            @Param("toCity") String toCity,
            @Param("toCountry") String toCountry);

    // Custom query to find RFQs by budget range
    @Query("SELECT lr FROM LogisticsRFQ lr WHERE " +
            "lr.budgetAmount BETWEEN :minBudget AND :maxBudget AND " +
            "lr.isActive = true")
    List<LogisticsRFQ> findByBudgetRange(
            @Param("minBudget") Double minBudget,
            @Param("maxBudget") Double maxBudget);

    // Check if RFQ number exists
    boolean existsByRfqNumber(String rfqNumber);

    // Count by RFQ type
    long countByRfqType(LogisticsRFQ.RFQType rfqType);

    // Count by status
    long countByStatus(LogisticsRFQ.RFQStatus status);

    // Count by RFQ type and status
    long countByRfqTypeAndStatus(LogisticsRFQ.RFQType rfqType, LogisticsRFQ.RFQStatus status);

    // Count by requested by
    long countByRequestedBy(String requestedBy);

    // Count active RFQs
    long countByIsActiveTrue();

    // Count by RFQ type and active status
    long countByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType rfqType);
}