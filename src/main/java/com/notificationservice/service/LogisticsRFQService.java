package com.notificationservice.service;

import com.notificationservice.dto.LogisticsRFQDto;
import com.notificationservice.dto.RFQMappingDto;
import com.notificationservice.entity.LogisticsRFQ;
import com.notificationservice.entity.RFQMapping;
import com.notificationservice.mapper.LogisticsRFQMapper;
import com.notificationservice.mapper.RFQMappingMapper;
import com.notificationservice.repository.LogisticsRFQRepository;
import com.notificationservice.repository.RFQMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LogisticsRFQService {

    private final LogisticsRFQRepository logisticsRFQRepository;
    private final RFQMappingRepository rfqMappingRepository;
    private final LogisticsRFQMapper logisticsRFQMapper;
    private final RFQMappingMapper rfqMappingMapper;

    /**
     * Create a new RFQ
     */
    public LogisticsRFQDto createRFQ(LogisticsRFQDto rfqDto) {
        log.info("Creating new RFQ: {}", rfqDto.getTitle());

        // Generate unique RFQ number
        String rfqNumber = generateRFQNumber(rfqDto.getRfqType());
        rfqDto.setRfqNumber(rfqNumber);

        // Set default values
        if (rfqDto.getRequestedDate() == null) {
            rfqDto.setRequestedDate(LocalDateTime.now());
        }

        LogisticsRFQ rfq = logisticsRFQMapper.toEntity(rfqDto);
        LogisticsRFQ savedRfq = logisticsRFQRepository.save(rfq);

        log.info("Created RFQ with ID: {}", savedRfq.getId());
        return logisticsRFQMapper.toDto(savedRfq);
    }

    /**
     * Get RFQ by ID
     */
    @Transactional(readOnly = true)
    public Optional<LogisticsRFQDto> getRFQById(Long id) {
        log.info("Fetching RFQ by ID: {}", id);
        return logisticsRFQRepository.findById(id)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    // Load child and parent counts
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(id));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(id));
                    return dto;
                });
    }

    /**
     * Get RFQ by RFQ number
     */
    @Transactional(readOnly = true)
    public Optional<LogisticsRFQDto> getRFQByNumber(String rfqNumber) {
        log.info("Fetching RFQ by number: {}", rfqNumber);
        return logisticsRFQRepository.findByRfqNumber(rfqNumber)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Get all RFQs with pagination
     */
    @Transactional(readOnly = true)
    public Page<LogisticsRFQDto> getAllRFQs(Pageable pageable) {
        log.info("Fetching all RFQs with pagination");
        return logisticsRFQRepository.findByIsActiveTrue(pageable)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Get RFQs by type
     */
    @Transactional(readOnly = true)
    public Page<LogisticsRFQDto> getRFQsByType(LogisticsRFQ.RFQType rfqType, Pageable pageable) {
        log.info("Fetching RFQs by type: {}", rfqType);
        return logisticsRFQRepository.findByRfqTypeAndIsActiveTrue(rfqType, pageable)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Get RFQs by status
     */
    @Transactional(readOnly = true)
    public Page<LogisticsRFQDto> getRFQsByStatus(LogisticsRFQ.RFQStatus status, Pageable pageable) {
        log.info("Fetching RFQs by status: {}", status);
        return logisticsRFQRepository.findByStatusAndIsActiveTrue(status, pageable)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Get RFQs by requested by
     */
    @Transactional(readOnly = true)
    public Page<LogisticsRFQDto> getRFQsByRequestedBy(String requestedBy, Pageable pageable) {
        log.info("Fetching RFQs by requested by: {}", requestedBy);
        return logisticsRFQRepository.findByRequestedByAndIsActiveTrue(requestedBy, pageable)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Search RFQs by multiple criteria
     */
    @Transactional(readOnly = true)
    public Page<LogisticsRFQDto> searchRFQs(
            LogisticsRFQ.RFQType rfqType,
            LogisticsRFQ.RFQStatus status,
            String requestedBy,
            LogisticsRFQ.CargoType cargoType,
            LogisticsRFQ.TruckType truckType,
            LogisticsRFQ.TransportMode transportMode,
            LogisticsRFQ.Priority priority,
            Pageable pageable) {

        log.info("Searching RFQs with criteria: type={}, status={}, requestedBy={}", rfqType, status, requestedBy);

        return logisticsRFQRepository.findByMultipleCriteria(
                rfqType, status, requestedBy, cargoType, truckType, transportMode, priority, pageable)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                });
    }

    /**
     * Update RFQ
     */
    public Optional<LogisticsRFQDto> updateRFQ(Long id, LogisticsRFQDto rfqDto) {
        log.info("Updating RFQ with ID: {}", id);

        return logisticsRFQRepository.findById(id)
                .map(existingRfq -> {
                    // Preserve immutable fields
                    rfqDto.setId(id);
                    rfqDto.setRfqNumber(existingRfq.getRfqNumber());
                    rfqDto.setCreatedAt(existingRfq.getCreatedAt());
                    rfqDto.setCreatedBy(existingRfq.getCreatedBy());
                    rfqDto.setUpdatedAt(LocalDateTime.now());

                    LogisticsRFQ updatedRfq = logisticsRFQMapper.toEntity(rfqDto);
                    LogisticsRFQ savedRfq = logisticsRFQRepository.save(updatedRfq);

                    log.info("Updated RFQ with ID: {}", id);
                    return logisticsRFQMapper.toDto(savedRfq);
                });
    }

    /**
     * Delete RFQ (soft delete)
     */
    public boolean deleteRFQ(Long id) {
        log.info("Deleting RFQ with ID: {}", id);

        return logisticsRFQRepository.findById(id)
                .map(rfq -> {
                    rfq.setIsActive(false);
                    rfq.setUpdatedAt(LocalDateTime.now());
                    logisticsRFQRepository.save(rfq);
                    log.info("Deleted RFQ with ID: {}", id);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Submit RFQ for approval (for DEMAND type)
     */
    public Optional<LogisticsRFQDto> submitRFQ(Long id) {
        log.info("Submitting RFQ with ID: {}", id);

        return logisticsRFQRepository.findById(id)
                .map(rfq -> {
                    if (rfq.getRfqType() == LogisticsRFQ.RFQType.DEMAND &&
                            rfq.getStatus() == LogisticsRFQ.RFQStatus.DRAFT) {
                        rfq.setStatus(LogisticsRFQ.RFQStatus.SUBMITTED);
                        rfq.setUpdatedAt(LocalDateTime.now());
                        LogisticsRFQ savedRfq = logisticsRFQRepository.save(rfq);
                        log.info("Submitted RFQ with ID: {}", id);
                        return logisticsRFQMapper.toDto(savedRfq);
                    } else {
                        log.warn("Cannot submit RFQ with ID: {}. Invalid type or status.", id);
                        return logisticsRFQMapper.toDto(rfq);
                    }
                });
    }

    /**
     * Publish RFQ
     */
    public Optional<LogisticsRFQDto> publishRFQ(Long id) {
        log.info("Publishing RFQ with ID: {}", id);

        return logisticsRFQRepository.findById(id)
                .map(rfq -> {
                    if (rfq.getStatus() == LogisticsRFQ.RFQStatus.DRAFT ||
                            rfq.getStatus() == LogisticsRFQ.RFQStatus.APPROVED) {
                        rfq.setStatus(LogisticsRFQ.RFQStatus.PUBLISHED);
                        rfq.setUpdatedAt(LocalDateTime.now());
                        LogisticsRFQ savedRfq = logisticsRFQRepository.save(rfq);
                        log.info("Published RFQ with ID: {}", id);
                        return logisticsRFQMapper.toDto(savedRfq);
                    } else {
                        log.warn("Cannot publish RFQ with ID: {}. Invalid status.", id);
                        return logisticsRFQMapper.toDto(rfq);
                    }
                });
    }

    /**
     * Award RFQ
     */
    public Optional<LogisticsRFQDto> awardRFQ(Long id) {
        log.info("Awarding RFQ with ID: {}", id);

        return logisticsRFQRepository.findById(id)
                .map(rfq -> {
                    if (rfq.getStatus() == LogisticsRFQ.RFQStatus.IN_PROGRESS) {
                        rfq.setStatus(LogisticsRFQ.RFQStatus.AWARDED);
                        rfq.setUpdatedAt(LocalDateTime.now());
                        LogisticsRFQ savedRfq = logisticsRFQRepository.save(rfq);
                        log.info("Awarded RFQ with ID: {}", id);
                        return logisticsRFQMapper.toDto(savedRfq);
                    } else {
                        log.warn("Cannot award RFQ with ID: {}. Invalid status.", id);
                        return logisticsRFQMapper.toDto(rfq);
                    }
                });
    }

    /**
     * Get child RFQs
     */
    @Transactional(readOnly = true)
    public List<LogisticsRFQDto> getChildRFQs(Long parentId) {
        log.info("Fetching child RFQs for parent ID: {}", parentId);

        List<RFQMapping> mappings = rfqMappingRepository.findActiveChildrenByParentId(parentId);
        return mappings.stream()
                .map(mapping -> logisticsRFQRepository.findById(mapping.getChildRfqId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                })
                .toList();
    }

    /**
     * Get parent RFQs
     */
    @Transactional(readOnly = true)
    public List<LogisticsRFQDto> getParentRFQs(Long childId) {
        log.info("Fetching parent RFQs for child ID: {}", childId);

        List<RFQMapping> mappings = rfqMappingRepository.findActiveParentsByChildId(childId);
        return mappings.stream()
                .map(mapping -> logisticsRFQRepository.findById(mapping.getParentRfqId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(rfq -> {
                    LogisticsRFQDto dto = logisticsRFQMapper.toDto(rfq);
                    dto.setChildRfqCount((int) rfqMappingRepository.countByParentRfqIdAndIsActiveTrue(rfq.getId()));
                    dto.setParentRfqCount((int) rfqMappingRepository.countByChildRfqIdAndIsActiveTrue(rfq.getId()));
                    return dto;
                })
                .toList();
    }

    /**
     * Create child RFQ with mapping
     */
    public LogisticsRFQDto createChildRFQ(Long parentId, LogisticsRFQDto childRfqDto,
            RFQMapping.RelationshipType relationshipType) {
        log.info("Creating child RFQ for parent ID: {} with relationship type: {}", parentId, relationshipType);

        // Create the child RFQ
        LogisticsRFQDto createdChildRfq = createRFQ(childRfqDto);

        // Create the mapping
        RFQMapping mapping = new RFQMapping();
        mapping.setParentRfqId(parentId);
        mapping.setChildRfqId(createdChildRfq.getId());
        mapping.setRelationshipType(relationshipType);
        mapping.setSequenceOrder(childRfqDto.getSequenceOrder());
        mapping.setIsActive(true);

        rfqMappingRepository.save(mapping);

        log.info("Created child RFQ with ID: {} for parent ID: {}", createdChildRfq.getId(), parentId);
        return createdChildRfq;
    }

    /**
     * Generate unique RFQ number
     */
    private String generateRFQNumber(LogisticsRFQ.RFQType rfqType) {
        String prefix = switch (rfqType) {
            case DEMAND -> "DEM";
            case SEGMENT -> "SEG";
            case REQUISITION -> "REQ";
        };

        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        return prefix + "-" + timestamp + "-" + random;
    }

    /**
     * Get statistics
     */
    @Transactional(readOnly = true)
    public RFQStatistics getStatistics() {
        log.info("Fetching RFQ statistics");

        return RFQStatistics.builder()
                .totalRFQs(logisticsRFQRepository.countByIsActiveTrue())
                .demandRFQs(logisticsRFQRepository.countByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType.DEMAND))
                .segmentRFQs(logisticsRFQRepository.countByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType.SEGMENT))
                .requisitionRFQs(logisticsRFQRepository.countByRfqTypeAndIsActiveTrue(LogisticsRFQ.RFQType.REQUISITION))
                .draftRFQs(logisticsRFQRepository.countByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus.DRAFT))
                .publishedRFQs(logisticsRFQRepository.countByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus.PUBLISHED))
                .awardedRFQs(logisticsRFQRepository.countByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus.AWARDED))
                .completedRFQs(logisticsRFQRepository.countByStatusAndIsActiveTrue(LogisticsRFQ.RFQStatus.COMPLETED))
                .build();
    }

    /**
     * Statistics DTO
     */
    public static class RFQStatistics {
        private long totalRFQs;
        private long demandRFQs;
        private long segmentRFQs;
        private long requisitionRFQs;
        private long draftRFQs;
        private long publishedRFQs;
        private long awardedRFQs;
        private long completedRFQs;

        // Builder pattern
        public static RFQStatisticsBuilder builder() {
            return new RFQStatisticsBuilder();
        }

        public static class RFQStatisticsBuilder {
            private RFQStatistics statistics = new RFQStatistics();

            public RFQStatisticsBuilder totalRFQs(long totalRFQs) {
                statistics.totalRFQs = totalRFQs;
                return this;
            }

            public RFQStatisticsBuilder demandRFQs(long demandRFQs) {
                statistics.demandRFQs = demandRFQs;
                return this;
            }

            public RFQStatisticsBuilder segmentRFQs(long segmentRFQs) {
                statistics.segmentRFQs = segmentRFQs;
                return this;
            }

            public RFQStatisticsBuilder requisitionRFQs(long requisitionRFQs) {
                statistics.requisitionRFQs = requisitionRFQs;
                return this;
            }

            public RFQStatisticsBuilder draftRFQs(long draftRFQs) {
                statistics.draftRFQs = draftRFQs;
                return this;
            }

            public RFQStatisticsBuilder publishedRFQs(long publishedRFQs) {
                statistics.publishedRFQs = publishedRFQs;
                return this;
            }

            public RFQStatisticsBuilder awardedRFQs(long awardedRFQs) {
                statistics.awardedRFQs = awardedRFQs;
                return this;
            }

            public RFQStatisticsBuilder completedRFQs(long completedRFQs) {
                statistics.completedRFQs = completedRFQs;
                return this;
            }

            public RFQStatistics build() {
                return statistics;
            }
        }

        // Getters
        public long getTotalRFQs() {
            return totalRFQs;
        }

        public long getDemandRFQs() {
            return demandRFQs;
        }

        public long getSegmentRFQs() {
            return segmentRFQs;
        }

        public long getRequisitionRFQs() {
            return requisitionRFQs;
        }

        public long getDraftRFQs() {
            return draftRFQs;
        }

        public long getPublishedRFQs() {
            return publishedRFQs;
        }

        public long getAwardedRFQs() {
            return awardedRFQs;
        }

        public long getCompletedRFQs() {
            return completedRFQs;
        }
    }
}