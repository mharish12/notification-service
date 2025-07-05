package com.notificationservice.entity;

import com.notificationservice.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "logistics_rfq")
@EqualsAndHashCode(callSuper = true)
public class LogisticsRFQ extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_number", nullable = false, unique = true)
    private String rfqNumber;

    @Column(name = "rfq_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RFQType rfqType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    // Location Details (for all types)
    @Column(name = "ship_from_address")
    private String shipFromAddress;

    @Column(name = "ship_from_city")
    private String shipFromCity;

    @Column(name = "ship_from_state")
    private String shipFromState;

    @Column(name = "ship_from_country")
    private String shipFromCountry;

    @Column(name = "ship_from_postal_code")
    private String shipFromPostalCode;

    @Column(name = "ship_from_latitude")
    private BigDecimal shipFromLatitude;

    @Column(name = "ship_from_longitude")
    private BigDecimal shipFromLongitude;

    @Column(name = "ship_to_address")
    private String shipToAddress;

    @Column(name = "ship_to_city")
    private String shipToCity;

    @Column(name = "ship_to_state")
    private String shipToState;

    @Column(name = "ship_to_country")
    private String shipToCountry;

    @Column(name = "ship_to_postal_code")
    private String shipToPostalCode;

    @Column(name = "ship_to_latitude")
    private BigDecimal shipToLatitude;

    @Column(name = "ship_to_longitude")
    private BigDecimal shipToLongitude;

    // Cargo Details
    @Column(name = "total_weight")
    private BigDecimal totalWeight;

    @Column(name = "weight_unit")
    private String weightUnit = "KG";

    @Column(name = "total_volume")
    private BigDecimal totalVolume;

    @Column(name = "volume_unit")
    private String volumeUnit = "CBM";

    @Column(name = "cargo_type")
    @Enumerated(EnumType.STRING)
    private CargoType cargoType;

    @Column(name = "cargo_category")
    private String cargoCategory;

    @Column(name = "hazardous_material", nullable = false)
    private Boolean hazardousMaterial = false;

    @Column(name = "temperature_controlled", nullable = false)
    private Boolean temperatureControlled = false;

    @Column(name = "min_temperature")
    private BigDecimal minTemperature;

    @Column(name = "max_temperature")
    private BigDecimal maxTemperature;

    @Column(name = "pieces")
    private Integer pieces;

    // Transportation Requirements
    @Column(name = "truck_type")
    @Enumerated(EnumType.STRING)
    private TruckType truckType;

    @Column(name = "truck_capacity")
    private BigDecimal truckCapacity;

    @Column(name = "special_equipment")
    private String specialEquipment;

    @Column(name = "transport_mode")
    @Enumerated(EnumType.STRING)
    private TransportMode transportMode;

    @Column(name = "vehicle_count")
    private Integer vehicleCount = 1;

    // Timeline
    @Column(name = "pickup_date")
    private LocalDateTime pickupDate;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "flexible_pickup_window")
    private Boolean flexiblePickupWindow = false;

    @Column(name = "flexible_delivery_window")
    private Boolean flexibleDeliveryWindow = false;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Column(name = "estimated_distance_km")
    private BigDecimal estimatedDistanceKm;

    // Budget and Pricing
    @Column(name = "budget_amount")
    private BigDecimal budgetAmount;

    @Column(name = "currency", length = 3)
    private String currency = "USD";

    @Column(name = "pricing_model")
    @Enumerated(EnumType.STRING)
    private PricingModel pricingModel;

    @Column(name = "payment_terms")
    private String paymentTerms;

    // Provider Requirements
    @Column(name = "provider_type")
    @Enumerated(EnumType.STRING)
    private ProviderType providerType;

    @Column(name = "required_certifications")
    private String requiredCertifications;

    @Column(name = "insurance_requirements")
    private String insuranceRequirements;

    @Column(name = "experience_years")
    private Integer experienceYears;

    // Status and Priority
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RFQStatus status = RFQStatus.DRAFT;

    @Column(name = "priority", nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "requested_date", nullable = false)
    private LocalDateTime requestedDate;

    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    // RFQ Types
    public enum RFQType {
        DEMAND, // Initial demand RFQ
        SEGMENT, // Segment-specific RFQ
        REQUISITION // Requisition for transporter selection
    }

    // Cargo Types
    public enum CargoType {
        GENERAL_CARGO,
        BULK_CARGO,
        CONTAINERIZED,
        LIQUID_CARGO,
        GAS_CARGO,
        HEAVY_LIFT,
        OVERSIZED,
        REFRIGERATED,
        DANGEROUS_GOODS
    }

    // Truck Types
    public enum TruckType {
        FLATBED,
        BOX_TRUCK,
        REFRIGERATED,
        TANKER,
        CONTAINER_CHASSIS,
        LOWBOY,
        DUMP_TRUCK,
        PICKUP_TRUCK,
        VAN,
        SPECIALIZED
    }

    // Transport Modes
    public enum TransportMode {
        ROAD, // Truck transportation
        RAIL, // Train transportation
        AIR, // Air freight
        SEA, // Sea freight
        MULTIMODAL // Combination of modes
    }

    // Pricing Models
    public enum PricingModel {
        FIXED_PRICE, // Fixed price for the service
        PER_KM, // Price per kilometer
        PER_WEIGHT, // Price per weight unit
        PER_VOLUME, // Price per volume unit
        PER_HOUR, // Price per hour
        NEGOTIABLE // Negotiable pricing
    }

    // Provider Types
    public enum ProviderType {
        TRANSPORTER, // Transportation company
        FREIGHT_FORWARDER, // Freight forwarder
        LOGISTICS_PROVIDER, // Logistics provider
        WAREHOUSE, // Warehouse operator
        CUSTOMS_BROKER, // Customs broker
        INSURANCE_PROVIDER // Insurance provider
    }

    // RFQ Status
    public enum RFQStatus {
        DRAFT, // Initial draft state
        SUBMITTED, // Submitted for review (for DEMAND)
        APPROVED, // Approved and ready for RFQ (for DEMAND)
        PUBLISHED, // Published and open for responses
        IN_PROGRESS, // Responses received, being evaluated
        AWARDED, // Awarded to selected provider
        IN_TRANSIT, // Currently being transported (for SEGMENT)
        COMPLETED, // Successfully completed
        CANCELLED, // Cancelled
        EXPIRED, // Expired without award
        DELAYED // Delayed (for SEGMENT)
    }

    // Priority levels
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
}