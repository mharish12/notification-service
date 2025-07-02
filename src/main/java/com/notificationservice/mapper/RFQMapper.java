package com.notificationservice.mapper;

import com.notificationservice.dto.RFQDto;
import com.notificationservice.entity.RFQ;
import java.util.List;
import java.util.stream.Collectors;

public class RFQMapper {
    public static RFQDto toDto(RFQ rfq) {
        if (rfq == null)
            return null;
        return RFQDto.builder()
                .id(rfq.getId())
                .referenceNumber(rfq.getReferenceNumber())
                .title(rfq.getTitle())
                .description(rfq.getDescription())
                .state(rfq.getState())
                .createdAt(rfq.getCreatedAt())
                .updatedAt(rfq.getUpdatedAt())
                .build();
    }

    public static RFQ toEntity(RFQDto dto) {
        if (dto == null)
            return null;
        return RFQ.builder()
                .id(dto.getId())
                .referenceNumber(dto.getReferenceNumber())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .state(dto.getState())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static List<RFQDto> toDtoList(List<RFQ> rfqs) {
        return rfqs == null ? null : rfqs.stream().map(RFQMapper::toDto).collect(Collectors.toList());
    }

    public static List<RFQ> toEntityList(List<RFQDto> dtos) {
        return dtos == null ? null : dtos.stream().map(RFQMapper::toEntity).collect(Collectors.toList());
    }
}