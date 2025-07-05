package com.notificationservice.dto;

import com.notificationservice.entity.LogisticsRFQ;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class LogisticsRFQDto extends BaseAuditableDto {

    private Long id;
    private String rfqNumber;
    private LogisticsRFQ.RFQType rfqType;
    private String title;
    private String description;
    private Integer sequenceOrder;

    // Location Details
    private String shipFromAddress;
    private String shipFromCity;
    private String shipFromState;
    private String shipFromCountry;
    private String shipFromPostalCode;
    private BigDecimal shipFromLatitude;
    private BigDecimal shipFromLongitude;

    private String shipToAddress;
    private String shipToCity;
    private String shipToState;
    private String shipToCountry;
    private String shipToPostalCode;
    private BigDecimal shipToLatitude;
    private BigDecimal shipToLongitude;

    // Cargo Details
    private BigDecimal totalWeight;
    private String weightUnit;
    private BigDecimal totalVolume;
    private String volumeUnit;
    private LogisticsRFQ.CargoType cargoType;
    private String cargoCategory;
    private Boolean hazardousMaterial;
    private Boolean temperatureControlled;
    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private Integer pieces;

    // Transportation Requirements
    private LogisticsRFQ.TruckType truckType;
    private BigDecimal truckCapacity;
    private String specialEquipment;
    private LogisticsRFQ.TransportMode transportMode;
    private Integer vehicleCount;

    // Timeline
    private LocalDateTime pickupDate;
    private LocalDateTime deliveryDate;
    private Boolean flexiblePickupWindow;
    private Boolean flexibleDeliveryWindow;
    private Integer estimatedDurationHours;
    private BigDecimal estimatedDistanceKm;

    // Budget and Pricing
    private BigDecimal budgetAmount;
    private String currency;
    private LogisticsRFQ.PricingModel pricingModel;
    private String paymentTerms;

    // Provider Requirements
    private LogisticsRFQ.ProviderType providerType;
    private String requiredCertifications;
    private String insuranceRequirements;
    private Integer experienceYears;

    // Status and Priority
    private LogisticsRFQ.RFQStatus status;
    private LogisticsRFQ.Priority priority;
    private String requestedBy;
    private LocalDateTime requestedDate;
    private LocalDateTime validUntil;
    private Boolean isActive;
    private String metadata;

    // Relationships
    private List<LogisticsRFQDto> childRfqs;
    private List<LogisticsRFQDto> parentRfqs;

    // Additional fields for UI
    private String statusDisplay;
    private String priorityDisplay;
    private String rfqTypeDisplay;
    private String cargoTypeDisplay;
    private String truckTypeDisplay;
    private String transportModeDisplay;
    private String pricingModelDisplay;
    private String providerTypeDisplay;
    private Integer childRfqCount;
    private Integer parentRfqCount;
}