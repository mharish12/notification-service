package com.notificationservice.entity;

import com.notificationservice.entity.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "rfq_mapping")
@EqualsAndHashCode(callSuper = true)
public class RFQMapping extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_rfq_id", nullable = false)
    private Long parentRfqId;

    @Column(name = "child_rfq_id", nullable = false)
    private Long childRfqId;

    @Column(name = "relationship_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON string for additional data

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_rfq_id", insertable = false, updatable = false)
    private LogisticsRFQ parentRfq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_rfq_id", insertable = false, updatable = false)
    private LogisticsRFQ childRfq;

    // Relationship Types
    public enum RelationshipType {
        DEMAND_TO_SEGMENT, // Demand → Segment
        SEGMENT_TO_REQUISITION, // Segment → Requisition
        REQUISITION_TO_RFQ // Requisition → Logistics RFQ
    }
}