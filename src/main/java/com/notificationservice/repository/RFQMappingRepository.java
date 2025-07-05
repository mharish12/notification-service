package com.notificationservice.repository;

import com.notificationservice.entity.RFQMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RFQMappingRepository extends JpaRepository<RFQMapping, Long> {

    // Find by parent RFQ ID
    List<RFQMapping> findByParentRfqId(Long parentRfqId);

    // Find by child RFQ ID
    List<RFQMapping> findByChildRfqId(Long childRfqId);

    // Find by relationship type
    List<RFQMapping> findByRelationshipType(RFQMapping.RelationshipType relationshipType);

    // Find by parent RFQ ID and relationship type
    List<RFQMapping> findByParentRfqIdAndRelationshipType(Long parentRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Find by child RFQ ID and relationship type
    List<RFQMapping> findByChildRfqIdAndRelationshipType(Long childRfqId, RFQMapping.RelationshipType relationshipType);

    // Find by parent RFQ ID and active status
    List<RFQMapping> findByParentRfqIdAndIsActiveTrue(Long parentRfqId);

    // Find by child RFQ ID and active status
    List<RFQMapping> findByChildRfqIdAndIsActiveTrue(Long childRfqId);

    // Find by relationship type and active status
    List<RFQMapping> findByRelationshipTypeAndIsActiveTrue(RFQMapping.RelationshipType relationshipType);

    // Find all active mappings
    List<RFQMapping> findByIsActiveTrue();

    // Find by parent RFQ ID, relationship type and active status
    List<RFQMapping> findByParentRfqIdAndRelationshipTypeAndIsActiveTrue(Long parentRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Find by child RFQ ID, relationship type and active status
    List<RFQMapping> findByChildRfqIdAndRelationshipTypeAndIsActiveTrue(Long childRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Find by parent RFQ ID ordered by sequence
    List<RFQMapping> findByParentRfqIdOrderBySequenceOrderAsc(Long parentRfqId);

    // Find by parent RFQ ID and active status ordered by sequence
    List<RFQMapping> findByParentRfqIdAndIsActiveTrueOrderBySequenceOrderAsc(Long parentRfqId);

    // Find by parent RFQ ID and relationship type ordered by sequence
    List<RFQMapping> findByParentRfqIdAndRelationshipTypeOrderBySequenceOrderAsc(Long parentRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Find by parent RFQ ID, relationship type and active status ordered by
    // sequence
    List<RFQMapping> findByParentRfqIdAndRelationshipTypeAndIsActiveTrueOrderBySequenceOrderAsc(Long parentRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Custom query to find all child RFQs for a parent
    @Query("SELECT rm FROM RFQMapping rm WHERE rm.parentRfqId = :parentRfqId AND rm.isActive = true ORDER BY rm.sequenceOrder ASC")
    List<RFQMapping> findActiveChildrenByParentId(@Param("parentRfqId") Long parentRfqId);

    // Custom query to find all parent RFQs for a child
    @Query("SELECT rm FROM RFQMapping rm WHERE rm.childRfqId = :childRfqId AND rm.isActive = true")
    List<RFQMapping> findActiveParentsByChildId(@Param("childRfqId") Long childRfqId);

    // Custom query to find mappings by relationship type
    @Query("SELECT rm FROM RFQMapping rm WHERE rm.relationshipType = :relationshipType AND rm.isActive = true")
    List<RFQMapping> findActiveByRelationshipType(
            @Param("relationshipType") RFQMapping.RelationshipType relationshipType);

    // Check if mapping exists
    boolean existsByParentRfqIdAndChildRfqIdAndRelationshipType(Long parentRfqId, Long childRfqId,
            RFQMapping.RelationshipType relationshipType);

    // Count by parent RFQ ID
    long countByParentRfqId(Long parentRfqId);

    // Count by child RFQ ID
    long countByChildRfqId(Long childRfqId);

    // Count by relationship type
    long countByRelationshipType(RFQMapping.RelationshipType relationshipType);

    // Count by parent RFQ ID and relationship type
    long countByParentRfqIdAndRelationshipType(Long parentRfqId, RFQMapping.RelationshipType relationshipType);

    // Count by child RFQ ID and relationship type
    long countByChildRfqIdAndRelationshipType(Long childRfqId, RFQMapping.RelationshipType relationshipType);

    // Count active mappings by parent RFQ ID
    long countByParentRfqIdAndIsActiveTrue(Long parentRfqId);

    // Count active mappings by child RFQ ID
    long countByChildRfqIdAndIsActiveTrue(Long childRfqId);

    // Count active mappings by relationship type
    long countByRelationshipTypeAndIsActiveTrue(RFQMapping.RelationshipType relationshipType);
}