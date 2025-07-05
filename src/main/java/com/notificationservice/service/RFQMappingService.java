package com.notificationservice.service;

import com.notificationservice.dto.RFQMappingDto;
import com.notificationservice.entity.LogisticsRFQ;
import com.notificationservice.entity.RFQMapping;
import com.notificationservice.mapper.RFQMappingMapper;
import com.notificationservice.repository.LogisticsRFQRepository;
import com.notificationservice.repository.RFQMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RFQMappingService {

    private final RFQMappingRepository rfqMappingRepository;
    private final LogisticsRFQRepository logisticsRFQRepository;
    private final RFQMappingMapper rfqMappingMapper;

    /**
     * Create a new RFQ mapping
     */
    public RFQMappingDto createMapping(RFQMappingDto mappingDto) {
        log.info("Creating RFQ mapping: parent={}, child={}, type={}",
                mappingDto.getParentRfqId(), mappingDto.getChildRfqId(), mappingDto.getRelationshipType());

        // Validate that both RFQs exist
        if (!logisticsRFQRepository.existsById(mappingDto.getParentRfqId())) {
            throw new IllegalArgumentException("Parent RFQ not found with ID: " + mappingDto.getParentRfqId());
        }

        if (!logisticsRFQRepository.existsById(mappingDto.getChildRfqId())) {
            throw new IllegalArgumentException("Child RFQ not found with ID: " + mappingDto.getChildRfqId());
        }

        // Check if mapping already exists
        if (rfqMappingRepository.existsByParentRfqIdAndChildRfqIdAndRelationshipType(
                mappingDto.getParentRfqId(), mappingDto.getChildRfqId(), mappingDto.getRelationshipType())) {
            throw new IllegalArgumentException("Mapping already exists for this parent-child relationship");
        }

        RFQMapping mapping = rfqMappingMapper.toEntity(mappingDto);
        mapping.setIsActive(true);

        RFQMapping savedMapping = rfqMappingRepository.save(mapping);
        log.info("Created RFQ mapping with ID: {}", savedMapping.getId());

        return rfqMappingMapper.toDto(savedMapping);
    }

    /**
     * Get mapping by ID
     */
    @Transactional(readOnly = true)
    public Optional<RFQMappingDto> getMappingById(Long id) {
        log.info("Fetching RFQ mapping by ID: {}", id);
        return rfqMappingRepository.findById(id)
                .map(rfqMappingMapper::toDto);
    }

    /**
     * Get all mappings
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getAllMappings() {
        log.info("Fetching all RFQ mappings");
        return rfqMappingRepository.findByIsActiveTrue()
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Get mappings by parent RFQ ID
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getMappingsByParentId(Long parentId) {
        log.info("Fetching RFQ mappings by parent ID: {}", parentId);
        return rfqMappingRepository.findByParentRfqIdAndIsActiveTrueOrderBySequenceOrderAsc(parentId)
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Get mappings by child RFQ ID
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getMappingsByChildId(Long childId) {
        log.info("Fetching RFQ mappings by child ID: {}", childId);
        return rfqMappingRepository.findByChildRfqIdAndIsActiveTrue(childId)
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Get mappings by relationship type
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getMappingsByRelationshipType(RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by relationship type: {}", relationshipType);
        return rfqMappingRepository.findByRelationshipTypeAndIsActiveTrue(relationshipType)
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Get mappings by parent ID and relationship type
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getMappingsByParentIdAndType(Long parentId,
            RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by parent ID: {} and type: {}", parentId, relationshipType);
        return rfqMappingRepository
                .findByParentRfqIdAndRelationshipTypeAndIsActiveTrueOrderBySequenceOrderAsc(parentId, relationshipType)
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Get mappings by child ID and relationship type
     */
    @Transactional(readOnly = true)
    public List<RFQMappingDto> getMappingsByChildIdAndType(Long childId, RFQMapping.RelationshipType relationshipType) {
        log.info("Fetching RFQ mappings by child ID: {} and type: {}", childId, relationshipType);
        return rfqMappingRepository.findByChildRfqIdAndRelationshipTypeAndIsActiveTrue(childId, relationshipType)
                .stream()
                .map(rfqMappingMapper::toDto)
                .toList();
    }

    /**
     * Update mapping
     */
    public Optional<RFQMappingDto> updateMapping(Long id, RFQMappingDto mappingDto) {
        log.info("Updating RFQ mapping with ID: {}", id);

        return rfqMappingRepository.findById(id)
                .map(existingMapping -> {
                    // Preserve immutable fields
                    mappingDto.setId(id);
                    mappingDto.setCreatedAt(existingMapping.getCreatedAt());
                    mappingDto.setCreatedBy(existingMapping.getCreatedBy());
                    mappingDto.setUpdatedAt(LocalDateTime.now());

                    RFQMapping updatedMapping = rfqMappingMapper.toEntity(mappingDto);
                    RFQMapping savedMapping = rfqMappingRepository.save(updatedMapping);

                    log.info("Updated RFQ mapping with ID: {}", id);
                    return rfqMappingMapper.toDto(savedMapping);
                });
    }

    /**
     * Delete mapping (soft delete)
     */
    public boolean deleteMapping(Long id) {
        log.info("Deleting RFQ mapping with ID: {}", id);

        return rfqMappingRepository.findById(id)
                .map(mapping -> {
                    mapping.setIsActive(false);
                    mapping.setUpdatedAt(LocalDateTime.now());
                    rfqMappingRepository.save(mapping);
                    log.info("Deleted RFQ mapping with ID: {}", id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Delete mapping by parent and child IDs
     */
    public boolean deleteMappingByParentAndChild(Long parentId, Long childId,
            RFQMapping.RelationshipType relationshipType) {
        log.info("Deleting RFQ mapping: parent={}, child={}, type={}", parentId, childId, relationshipType);

        return rfqMappingRepository
                .findByParentRfqIdAndChildRfqIdAndRelationshipType(parentId, childId, relationshipType)
                .stream()
                .findFirst()
                .map(mapping -> {
                    mapping.setIsActive(false);
                    mapping.setUpdatedAt(LocalDateTime.now());
                    rfqMappingRepository.save(mapping);
                    log.info("Deleted RFQ mapping: parent={}, child={}, type={}", parentId, childId, relationshipType);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Create demand to segment mapping
     */
    public RFQMappingDto createDemandToSegmentMapping(Long demandId, Long segmentId, Integer sequenceOrder) {
        log.info("Creating demand to segment mapping: demand={}, segment={}, sequence={}", demandId, segmentId,
                sequenceOrder);

        RFQMappingDto mappingDto = new RFQMappingDto();
        mappingDto.setParentRfqId(demandId);
        mappingDto.setChildRfqId(segmentId);
        mappingDto.setRelationshipType(RFQMapping.RelationshipType.DEMAND_TO_SEGMENT);
        mappingDto.setSequenceOrder(sequenceOrder);

        return createMapping(mappingDto);
    }

    /**
     * Create segment to requisition mapping
     */
    public RFQMappingDto createSegmentToRequisitionMapping(Long segmentId, Long requisitionId, Integer sequenceOrder) {
        log.info("Creating segment to requisition mapping: segment={}, requisition={}, sequence={}", segmentId,
                requisitionId, sequenceOrder);

        RFQMappingDto mappingDto = new RFQMappingDto();
        mappingDto.setParentRfqId(segmentId);
        mappingDto.setChildRfqId(requisitionId);
        mappingDto.setRelationshipType(RFQMapping.RelationshipType.SEGMENT_TO_REQUISITION);
        mappingDto.setSequenceOrder(sequenceOrder);

        return createMapping(mappingDto);
    }

    /**
     * Create requisition to RFQ mapping
     */
    public RFQMappingDto createRequisitionToRFQMapping(Long requisitionId, Long rfqId, Integer sequenceOrder) {
        log.info("Creating requisition to RFQ mapping: requisition={}, rfq={}, sequence={}", requisitionId, rfqId,
                sequenceOrder);

        RFQMappingDto mappingDto = new RFQMappingDto();
        mappingDto.setParentRfqId(requisitionId);
        mappingDto.setChildRfqId(rfqId);
        mappingDto.setRelationshipType(RFQMapping.RelationshipType.REQUISITION_TO_RFQ);
        mappingDto.setSequenceOrder(sequenceOrder);

        return createMapping(mappingDto);
    }

    /**
     * Get complete hierarchy for a demand
     */
    @Transactional(readOnly = true)
    public DemandHierarchy getDemandHierarchy(Long demandId) {
        log.info("Fetching complete hierarchy for demand ID: {}", demandId);

        // Get the demand
        Optional<LogisticsRFQ> demandOpt = logisticsRFQRepository.findById(demandId);
        if (demandOpt.isEmpty() || demandOpt.get().getRfqType() != LogisticsRFQ.RFQType.DEMAND) {
            throw new IllegalArgumentException("Demand not found with ID: " + demandId);
        }

        LogisticsRFQ demand = demandOpt.get();

        // Get segments
        List<RFQMapping> segmentMappings = rfqMappingRepository
                .findByParentRfqIdAndRelationshipTypeAndIsActiveTrueOrderBySequenceOrderAsc(demandId,
                        RFQMapping.RelationshipType.DEMAND_TO_SEGMENT);

        List<SegmentInfo> segments = segmentMappings.stream()
                .map(mapping -> {
                    Optional<LogisticsRFQ> segmentOpt = logisticsRFQRepository.findById(mapping.getChildRfqId());
                    if (segmentOpt.isPresent()) {
                        LogisticsRFQ segment = segmentOpt.get();

                        // Get requisitions for this segment
                        List<RFQMapping> requisitionMappings = rfqMappingRepository
                                .findByParentRfqIdAndRelationshipTypeAndIsActiveTrueOrderBySequenceOrderAsc(
                                        segment.getId(), RFQMapping.RelationshipType.SEGMENT_TO_REQUISITION);

                        List<RequisitionInfo> requisitions = requisitionMappings.stream()
                                .map(reqMapping -> {
                                    Optional<LogisticsRFQ> requisitionOpt = logisticsRFQRepository
                                            .findById(reqMapping.getChildRfqId());
                                    if (requisitionOpt.isPresent()) {
                                        LogisticsRFQ requisition = requisitionOpt.get();

                                        // Get individual RFQs for this requisition
                                        List<RFQMapping> rfqMappings = rfqMappingRepository
                                                .findByParentRfqIdAndRelationshipTypeAndIsActiveTrueOrderBySequenceOrderAsc(
                                                        requisition.getId(),
                                                        RFQMapping.RelationshipType.REQUISITION_TO_RFQ);

                                        List<Long> individualRfqIds = rfqMappings.stream()
                                                .map(RFQMapping::getChildRfqId)
                                                .toList();

                                        return new RequisitionInfo(requisition.getId(), requisition.getTitle(),
                                                requisition.getStatus(), reqMapping.getSequenceOrder(),
                                                individualRfqIds);
                                    }
                                    return null;
                                })
                                .filter(req -> req != null)
                                .toList();

                        return new SegmentInfo(segment.getId(), segment.getTitle(), segment.getStatus(),
                                mapping.getSequenceOrder(), requisitions);
                    }
                    return null;
                })
                .filter(seg -> seg != null)
                .toList();

        return new DemandHierarchy(demand.getId(), demand.getTitle(), demand.getStatus(), segments);
    }

    /**
     * Demand hierarchy DTO
     */
    public static class DemandHierarchy {
        private Long demandId;
        private String demandTitle;
        private LogisticsRFQ.RFQStatus demandStatus;
        private List<SegmentInfo> segments;

        public DemandHierarchy(Long demandId, String demandTitle, LogisticsRFQ.RFQStatus demandStatus,
                List<SegmentInfo> segments) {
            this.demandId = demandId;
            this.demandTitle = demandTitle;
            this.demandStatus = demandStatus;
            this.segments = segments;
        }

        // Getters
        public Long getDemandId() {
            return demandId;
        }

        public String getDemandTitle() {
            return demandTitle;
        }

        public LogisticsRFQ.RFQStatus getDemandStatus() {
            return demandStatus;
        }

        public List<SegmentInfo> getSegments() {
            return segments;
        }
    }

    /**
     * Segment info DTO
     */
    public static class SegmentInfo {
        private Long segmentId;
        private String segmentTitle;
        private LogisticsRFQ.RFQStatus segmentStatus;
        private Integer sequenceOrder;
        private List<RequisitionInfo> requisitions;

        public SegmentInfo(Long segmentId, String segmentTitle, LogisticsRFQ.RFQStatus segmentStatus,
                Integer sequenceOrder, List<RequisitionInfo> requisitions) {
            this.segmentId = segmentId;
            this.segmentTitle = segmentTitle;
            this.segmentStatus = segmentStatus;
            this.sequenceOrder = sequenceOrder;
            this.requisitions = requisitions;
        }

        // Getters
        public Long getSegmentId() {
            return segmentId;
        }

        public String getSegmentTitle() {
            return segmentTitle;
        }

        public LogisticsRFQ.RFQStatus getSegmentStatus() {
            return segmentStatus;
        }

        public Integer getSequenceOrder() {
            return sequenceOrder;
        }

        public List<RequisitionInfo> getRequisitions() {
            return requisitions;
        }
    }

    /**
     * Requisition info DTO
     */
    public static class RequisitionInfo {
        private Long requisitionId;
        private String requisitionTitle;
        private LogisticsRFQ.RFQStatus requisitionStatus;
        private Integer sequenceOrder;
        private List<Long> individualRfqIds;

        public RequisitionInfo(Long requisitionId, String requisitionTitle, LogisticsRFQ.RFQStatus requisitionStatus,
                Integer sequenceOrder, List<Long> individualRfqIds) {
            this.requisitionId = requisitionId;
            this.requisitionTitle = requisitionTitle;
            this.requisitionStatus = requisitionStatus;
            this.sequenceOrder = sequenceOrder;
            this.individualRfqIds = individualRfqIds;
        }

        // Getters
        public Long getRequisitionId() {
            return requisitionId;
        }

        public String getRequisitionTitle() {
            return requisitionTitle;
        }

        public LogisticsRFQ.RFQStatus getRequisitionStatus() {
            return requisitionStatus;
        }

        public Integer getSequenceOrder() {
            return sequenceOrder;
        }

        public List<Long> getIndividualRfqIds() {
            return individualRfqIds;
        }
    }
}