package com.notificationservice.controller;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.aspect.RateLimitType;
import com.notificationservice.dto.RFQMappingDto;
import com.notificationservice.entity.RFQMapping;
import com.notificationservice.service.RFQMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rfq-mappings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RFQ Mapping", description = "RFQ relationship mapping endpoints")
public class RFQMappingController {

    private final RFQMappingService rfqMappingService;

    /**
     * Create a new RFQ mapping
     */
    @PostMapping
    @RateLimited(type = RateLimitType.CREATE)
    @Operation(summary = "Create RFQ mapping", description = "Creates a new relationship mapping between RFQs")
    public ResponseEntity<RFQMappingDto> createMapping(
            @Parameter(description = "Mapping data") @Valid @RequestBody RFQMappingDto mappingDto) {
        log.info("Creating RFQ mapping: parent={}, child={}, type={}",
                mappingDto.getParentRfqId(), mappingDto.getChildRfqId(), mappingDto.getRelationshipType());
        RFQMappingDto createdMapping = rfqMappingService.createMapping(mappingDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMapping);
    }

    /**
     * Get mapping by ID
     */
    @GetMapping("/{id}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mapping by ID", description = "Retrieves an RFQ mapping by its ID")
    public ResponseEntity<RFQMappingDto> getMappingById(
            @Parameter(description = "Mapping ID") @PathVariable Long id) {
        log.info("Fetching RFQ mapping by ID: {}", id);
        Optional<RFQMappingDto> mapping = rfqMappingService.getMappingById(id);
        return mapping.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all mappings
     */
    @GetMapping
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get all mappings", description = "Retrieves all RFQ mappings")
    public ResponseEntity<List<RFQMappingDto>> getAllMappings() {
        log.info("Fetching all RFQ mappings");
        List<RFQMappingDto> mappings = rfqMappingService.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mappings by parent RFQ ID
     */
    @GetMapping("/parent/{parentId}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mappings by parent", description = "Retrieves all mappings for a parent RFQ")
    public ResponseEntity<List<RFQMappingDto>> getMappingsByParentId(
            @Parameter(description = "Parent RFQ ID") @PathVariable Long parentId) {
        log.info("Fetching RFQ mappings by parent ID: {}", parentId);
        List<RFQMappingDto> mappings = rfqMappingService.getMappingsByParentId(parentId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mappings by child RFQ ID
     */
    @GetMapping("/child/{childId}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mappings by child", description = "Retrieves all mappings for a child RFQ")
    public ResponseEntity<List<RFQMappingDto>> getMappingsByChildId(
            @Parameter(description = "Child RFQ ID") @PathVariable Long childId) {
        log.info("Fetching RFQ mappings by child ID: {}", childId);
        List<RFQMappingDto> mappings = rfqMappingService.getMappingsByChildId(childId);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mappings by relationship type
     */
    @GetMapping("/type/{relationshipType}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mappings by type", description = "Retrieves all mappings by relationship type")
    public ResponseEntity<List<RFQMappingDto>> getMappingsByRelationshipType(
            @Parameter(description = "Relationship type") @PathVariable RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by relationship type: {}", relationshipType);
        List<RFQMappingDto> mappings = rfqMappingService.getMappingsByRelationshipType(relationshipType);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mappings by parent ID and relationship type
     */
    @GetMapping("/parent/{parentId}/type/{relationshipType}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mappings by parent and type", description = "Retrieves mappings by parent ID and relationship type")
    public ResponseEntity<List<RFQMappingDto>> getMappingsByParentIdAndType(
            @Parameter(description = "Parent RFQ ID") @PathVariable Long parentId,
            @Parameter(description = "Relationship type") @PathVariable RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by parent ID: {} and type: {}", parentId, relationshipType);
        List<RFQMappingDto> mappings = rfqMappingService.getMappingsByParentIdAndType(parentId, relationshipType);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Get mappings by child ID and relationship type
     */
    @GetMapping("/child/{childId}/type/{relationshipType}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get mappings by child and type", description = "Retrieves mappings by child ID and relationship type")
    public ResponseEntity<List<RFQMappingDto>> getMappingsByChildIdAndType(
            @Parameter(description = "Child RFQ ID") @PathVariable Long childId,
            @Parameter(description = "Relationship type") @PathVariable RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by child ID: {} and type: {}", childId, relationshipType);
        List<RFQMappingDto> mappings = rfqMappingService.getMappingsByChildIdAndType(childId, relationshipType);
        return ResponseEntity.ok(mappings);
    }

    /**
     * Update mapping
     */
    @PutMapping("/{id}")
    @RateLimited(type = RateLimitType.ADMIN)
    @Operation(summary = "Update mapping", description = "Updates an existing RFQ mapping")
    public ResponseEntity<RFQMappingDto> updateMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id,
            @Parameter(description = "Updated mapping data") @Valid @RequestBody RFQMappingDto mappingDto) {
        log.info("Updating RFQ mapping with ID: {}", id);
        Optional<RFQMappingDto> updatedMapping = rfqMappingService.updateMapping(id, mappingDto);
        return updatedMapping.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete mapping
     */
    @DeleteMapping("/{id}")
    @RateLimited(type = RateLimitType.ADMIN)
    @Operation(summary = "Delete mapping", description = "Deletes an RFQ mapping (soft delete)")
    public ResponseEntity<Void> deleteMapping(
            @Parameter(description = "Mapping ID") @PathVariable Long id) {
        log.info("Deleting RFQ mapping with ID: {}", id);
        boolean deleted = rfqMappingService.deleteMapping(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Create demand to segment mapping
     */
    @PostMapping("/demand-to-segment")
    @RateLimited(type = RateLimitType.CREATE)
    @Operation(summary = "Create demand to segment mapping", description = "Creates a mapping from demand to segment")
    public ResponseEntity<RFQMappingDto> createDemandToSegmentMapping(
            @Parameter(description = "Demand RFQ ID") @RequestParam Long demandId,
            @Parameter(description = "Segment RFQ ID") @RequestParam Long segmentId,
            @Parameter(description = "Sequence order") @RequestParam Integer sequenceOrder) {
        log.info("Creating demand to segment mapping: demand={}, segment={}, sequence={}", demandId, segmentId,
                sequenceOrder);
        RFQMappingDto mapping = rfqMappingService.createDemandToSegmentMapping(demandId, segmentId, sequenceOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    /**
     * Create segment to requisition mapping
     */
    @PostMapping("/segment-to-requisition")
    @RateLimited(type = RateLimitType.CREATE)
    @Operation(summary = "Create segment to requisition mapping", description = "Creates a mapping from segment to requisition")
    public ResponseEntity<RFQMappingDto> createSegmentToRequisitionMapping(
            @Parameter(description = "Segment RFQ ID") @RequestParam Long segmentId,
            @Parameter(description = "Requisition RFQ ID") @RequestParam Long requisitionId,
            @Parameter(description = "Sequence order") @RequestParam Integer sequenceOrder) {
        log.info("Creating segment to requisition mapping: segment={}, requisition={}, sequence={}", segmentId,
                requisitionId, sequenceOrder);
        RFQMappingDto mapping = rfqMappingService.createSegmentToRequisitionMapping(segmentId, requisitionId,
                sequenceOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    /**
     * Create requisition to RFQ mapping
     */
    @PostMapping("/requisition-to-rfq")
    @RateLimited(type = RateLimitType.CREATE)
    @Operation(summary = "Create requisition to RFQ mapping", description = "Creates a mapping from requisition to individual RFQ")
    public ResponseEntity<RFQMappingDto> createRequisitionToRFQMapping(
            @Parameter(description = "Requisition RFQ ID") @RequestParam Long requisitionId,
            @Parameter(description = "Individual RFQ ID") @RequestParam Long rfqId,
            @Parameter(description = "Sequence order") @RequestParam Integer sequenceOrder) {
        log.info("Creating requisition to RFQ mapping: requisition={}, rfq={}, sequence={}", requisitionId, rfqId,
                sequenceOrder);
        RFQMappingDto mapping = rfqMappingService.createRequisitionToRFQMapping(requisitionId, rfqId, sequenceOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapping);
    }

    /**
     * Get complete hierarchy for a demand
     */
    @GetMapping("/hierarchy/{demandId}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get demand hierarchy", description = "Retrieves the complete hierarchy for a demand")
    public ResponseEntity<RFQMappingService.DemandHierarchy> getDemandHierarchy(
            @Parameter(description = "Demand RFQ ID") @PathVariable Long demandId) {
        log.info("Fetching complete hierarchy for demand ID: {}", demandId);
        RFQMappingService.DemandHierarchy hierarchy = rfqMappingService.getDemandHierarchy(demandId);
        return ResponseEntity.ok(hierarchy);
    }
}