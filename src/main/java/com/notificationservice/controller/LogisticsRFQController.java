package com.notificationservice.controller;

import com.notificationservice.annotation.RateLimited;
import com.notificationservice.aspect.RateLimitType;
import com.notificationservice.dto.LogisticsRFQDto;
import com.notificationservice.dto.RFQMappingDto;
import com.notificationservice.entity.LogisticsRFQ;
import com.notificationservice.entity.RFQMapping;
import com.notificationservice.service.LogisticsRFQService;
import com.notificationservice.service.RFQMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/rfqs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Logistics RFQ", description = "Logistics RFQ management endpoints")
public class LogisticsRFQController {

    private final LogisticsRFQService logisticsRFQService;
    private final RFQMappingService rfqMappingService;

    /**
     * Create a new RFQ
     */
    @PostMapping
    @RateLimited(type = RateLimitType.CREATE)
    @Operation(summary = "Create a new RFQ", description = "Creates a new logistics RFQ")
    public ResponseEntity<LogisticsRFQDto> createRFQ(
            @Parameter(description = "RFQ data") @Valid @RequestBody LogisticsRFQDto rfqDto) {
        log.info("Creating new RFQ: {}", rfqDto.getTitle());
        LogisticsRFQDto createdRfq = logisticsRFQService.createRFQ(rfqDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRfq);
    }

    /**
     * Get RFQ by ID
     */
    @GetMapping("/{id}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get RFQ by ID", description = "Retrieves an RFQ by its ID")
    public ResponseEntity<LogisticsRFQDto> getRFQById(
            @Parameter(description = "RFQ ID") @PathVariable Long id) {
        log.info("Fetching RFQ by ID: {}", id);
        Optional<LogisticsRFQDto> rfq = logisticsRFQService.getRFQById(id);
        return rfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get RFQ by RFQ number
     */
    @GetMapping("/number/{rfqNumber}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get RFQ by number", description = "Retrieves an RFQ by its RFQ number")
    public ResponseEntity<LogisticsRFQDto> getRFQByNumber(
            @Parameter(description = "RFQ number") @PathVariable String rfqNumber) {
        log.info("Fetching RFQ by number: {}", rfqNumber);
        Optional<LogisticsRFQDto> rfq = logisticsRFQService.getRFQByNumber(rfqNumber);
        return rfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all RFQs with pagination
     */
    @GetMapping
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get all RFQs", description = "Retrieves all RFQs with pagination")
    public ResponseEntity<Page<LogisticsRFQDto>> getAllRFQs(
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Fetching all RFQs with pagination");
        Page<LogisticsRFQDto> rfqs = logisticsRFQService.getAllRFQs(pageable);
        return ResponseEntity.ok(rfqs);
    }

    /**
     * Get RFQs by type
     */
    @GetMapping("/type/{rfqType}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get RFQs by type", description = "Retrieves RFQs by their type")
    public ResponseEntity<Page<LogisticsRFQDto>> getRFQsByType(
            @Parameter(description = "RFQ type") @PathVariable LogisticsRFQ.RFQType rfqType,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Fetching RFQs by type: {}", rfqType);
        Page<LogisticsRFQDto> rfqs = logisticsRFQService.getRFQsByType(rfqType, pageable);
        return ResponseEntity.ok(rfqs);
    }

    /**
     * Get RFQs by status
     */
    @GetMapping("/status/{status}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get RFQs by status", description = "Retrieves RFQs by their status")
    public ResponseEntity<Page<LogisticsRFQDto>> getRFQsByStatus(
            @Parameter(description = "RFQ status") @PathVariable LogisticsRFQ.RFQStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Fetching RFQs by status: {}", status);
        Page<LogisticsRFQDto> rfqs = logisticsRFQService.getRFQsByStatus(status, pageable);
        return ResponseEntity.ok(rfqs);
    }

    /**
     * Get RFQs by requested by
     */
    @GetMapping("/requested-by/{requestedBy}")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Get RFQs by requester", description = "Retrieves RFQs by the person who requested them")
    public ResponseEntity<Page<LogisticsRFQDto>> getRFQsByRequestedBy(
            @Parameter(description = "Requested by") @PathVariable String requestedBy,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Fetching RFQs by requested by: {}", requestedBy);
        Page<LogisticsRFQDto> rfqs = logisticsRFQService.getRFQsByRequestedBy(requestedBy, pageable);
        return ResponseEntity.ok(rfqs);
    }

    /**
     * Search RFQs by multiple criteria
     */
    @GetMapping("/search")
    @RateLimited(type = RateLimitType.READ)
    @Operation(summary = "Search RFQs", description = "Search RFQs by multiple criteria")
    public ResponseEntity<Page<LogisticsRFQDto>> searchRFQs(
            @Parameter(description = "RFQ type") @RequestParam(required = false) LogisticsRFQ.RFQType rfqType,
            @Parameter(description = "RFQ status") @RequestParam(required = false) LogisticsRFQ.RFQStatus status,
            @Parameter(description = "Requested by") @RequestParam(required = false) String requestedBy,
            @Parameter(description = "Cargo type") @RequestParam(required = false) LogisticsRFQ.CargoType cargoType,
            @Parameter(description = "Truck type") @RequestParam(required = false) LogisticsRFQ.TruckType truckType,
            @Parameter(description = "Transport mode") @RequestParam(required = false) LogisticsRFQ.TransportMode transportMode,
            @Parameter(description = "Priority") @RequestParam(required = false) LogisticsRFQ.Priority priority,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("Searching RFQs with criteria: type={}, status={}, requestedBy={}", rfqType, status, requestedBy);
        Page<LogisticsRFQDto> rfqs = logisticsRFQService.searchRFQs(
                rfqType, status, requestedBy, cargoType, truckType, transportMode, priority, pageable);
        return ResponseEntity.ok(rfqs);
    }

    /**
     * Update RFQ
     */
    @PutMapping("/{id}")
    @RateLimited(operation = "ADMIN")
    @Operation(summary = "Update RFQ", description = "Updates an existing RFQ")
    public ResponseEntity<LogisticsRFQDto> updateRFQ(
            @Parameter(description = "RFQ ID") @PathVariable Long id,
            @Parameter(description = "Updated RFQ data") @Valid @RequestBody LogisticsRFQDto rfqDto) {
        log.info("Updating RFQ with ID: {}", id);
        Optional<LogisticsRFQDto> updatedRfq = logisticsRFQService.updateRFQ(id, rfqDto);
        return updatedRfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete RFQ
     */
    @DeleteMapping("/{id}")
    @RateLimited(operation = "ADMIN")
    @Operation(summary = "Delete RFQ", description = "Deletes an RFQ (soft delete)")
    public ResponseEntity<Void> deleteRFQ(
            @Parameter(description = "RFQ ID") @PathVariable Long id) {
        log.info("Deleting RFQ with ID: {}", id);
        boolean deleted = logisticsRFQService.deleteRFQ(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Submit RFQ for approval
     */
    @PostMapping("/{id}/submit")
    @RateLimited(operation = "ADMIN")
    @Operation(summary = "Submit RFQ", description = "Submits an RFQ for approval (for DEMAND type)")
    public ResponseEntity<LogisticsRFQDto> submitRFQ(
            @Parameter(description = "RFQ ID") @PathVariable Long id) {
        log.info("Submitting RFQ with ID: {}", id);
        Optional<LogisticsRFQDto> submittedRfq = logisticsRFQService.submitRFQ(id);
        return submittedRfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publish RFQ
     */
    @PostMapping("/{id}/publish")
    @RateLimited(operation = "ADMIN")
    @Operation(summary = "Publish RFQ", description = "Publishes an RFQ for responses")
    public ResponseEntity<LogisticsRFQDto> publishRFQ(
            @Parameter(description = "RFQ ID") @PathVariable Long id) {
        log.info("Publishing RFQ with ID: {}", id);
        Optional<LogisticsRFQDto> publishedRfq = logisticsRFQService.publishRFQ(id);
        return publishedRfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Award RFQ
     */
    @PostMapping("/{id}/award")
    @RateLimited(operation = "ADMIN")
    @Operation(summary = "Award RFQ", description = "Awards an RFQ to a selected provider")
    public ResponseEntity<LogisticsRFQDto> awardRFQ(
            @Parameter(description = "RFQ ID") @PathVariable Long id) {
        log.info("Awarding RFQ with ID: {}", id);
        Optional<LogisticsRFQDto> awardedRfq = logisticsRFQService.awardRFQ(id);
        return awardedRfq.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get child RFQs
     */
    @GetMapping("/{id}/children")
    @RateLimited(operation = "READ")
    @Operation(summary = "Get child RFQs", description = "Retrieves child RFQs for a parent RFQ")
    public ResponseEntity<List<LogisticsRFQDto>> getChildRFQs(
            @Parameter(description = "Parent RFQ ID") @PathVariable Long id) {
        log.info("Fetching child RFQs for parent ID: {}", id);
        List<LogisticsRFQDto> childRfqs = logisticsRFQService.getChildRFQs(id);
        return ResponseEntity.ok(childRfqs);
    }

    /**
     * Get parent RFQs
     */
    @GetMapping("/{id}/parents")
    @RateLimited(operation = "READ")
    @Operation(summary = "Get parent RFQs", description = "Retrieves parent RFQs for a child RFQ")
    public ResponseEntity<List<LogisticsRFQDto>> getParentRFQs(
            @Parameter(description = "Child RFQ ID") @PathVariable Long id) {
        log.info("Fetching parent RFQs for child ID: {}", id);
        List<LogisticsRFQDto> parentRfqs = logisticsRFQService.getParentRFQs(id);
        return ResponseEntity.ok(parentRfqs);
    }

    /**
     * Create child RFQ
     */
    @PostMapping("/{id}/children")
    @RateLimited(operation = "CREATE")
    @Operation(summary = "Create child RFQ", description = "Creates a child RFQ with mapping to parent")
    public ResponseEntity<LogisticsRFQDto> createChildRFQ(
            @Parameter(description = "Parent RFQ ID") @PathVariable Long id,
            @Parameter(description = "Child RFQ data and relationship type") @Valid @RequestBody CreateChildRFQRequest request) {
        log.info("Creating child RFQ for parent ID: {} with relationship type: {}", id, request.getRelationshipType());
        LogisticsRFQDto childRfq = logisticsRFQService.createChildRFQ(id, request.getChildRfq(),
                request.getRelationshipType());
        return ResponseEntity.status(HttpStatus.CREATED).body(childRfq);
    }

    /**
     * Get RFQ statistics
     */
    @GetMapping("/statistics")
    @RateLimited(operation = "READ")
    @Operation(summary = "Get RFQ statistics", description = "Retrieves statistics about RFQs")
    public ResponseEntity<LogisticsRFQService.RFQStatistics> getStatistics() {
        log.info("Fetching RFQ statistics");
        LogisticsRFQService.RFQStatistics statistics = logisticsRFQService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Request DTO for creating child RFQ
     */
    public static class CreateChildRFQRequest {
        private LogisticsRFQDto childRfq;
        private RFQMapping.RelationshipType relationshipType;

        // Getters and setters
        public LogisticsRFQDto getChildRfq() {
            return childRfq;
        }

        public void setChildRfq(LogisticsRFQDto childRfq) {
            this.childRfq = childRfq;
        }

        public RFQMapping.RelationshipType getRelationshipType() {
            return relationshipType;
        }

        public void setRelationshipType(RFQMapping.RelationshipType relationshipType) {
            this.relationshipType = relationshipType;
        }
    }
}