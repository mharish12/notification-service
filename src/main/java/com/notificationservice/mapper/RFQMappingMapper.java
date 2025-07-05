package com.notificationservice.mapper;

import com.notificationservice.dto.RFQMappingDto;
import com.notificationservice.entity.RFQMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = { LogisticsRFQMapper.class })
public interface RFQMappingMapper {

    @Mapping(target = "parentRfq", ignore = true)
    @Mapping(target = "childRfq", ignore = true)
    @Mapping(target = "relationshipTypeDisplay", source = "relationshipType", qualifiedByName = "relationshipTypeToDisplay")
    RFQMappingDto toDto(RFQMapping entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    RFQMapping toEntity(RFQMappingDto dto);

    List<RFQMappingDto> toDtoList(List<RFQMapping> entities);

    List<RFQMapping> toEntityList(List<RFQMappingDto> dtos);

    @Named("relationshipTypeToDisplay")
    default String relationshipTypeToDisplay(RFQMapping.RelationshipType relationshipType) {
        if (relationshipType == null)
            return null;
        return relationshipType.name().replace("_", " ");
    }
}