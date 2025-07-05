package com.notificationservice.dto;

import com.notificationservice.entity.RFQMapping;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RFQMappingDto extends BaseAuditableDto {

    private Long id;
    private Long parentRfqId;
    private Long childRfqId;
    private RFQMapping.RelationshipType relationshipType;
    private Integer sequenceOrder;
    private Boolean isActive;
    private String metadata;

    // Additional fields for UI
    private String relationshipTypeDisplay;
    private LogisticsRFQDto parentRfq;
    private LogisticsRFQDto childRfq;
}