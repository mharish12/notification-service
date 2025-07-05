package com.notificationservice.mapper;

import com.notificationservice.dto.LogisticsRFQDto;
import com.notificationservice.entity.LogisticsRFQ;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LogisticsRFQMapper {

    @Mapping(target = "childRfqs", ignore = true)
    @Mapping(target = "parentRfqs", ignore = true)
    @Mapping(target = "statusDisplay", source = "status", qualifiedByName = "statusToDisplay")
    @Mapping(target = "priorityDisplay", source = "priority", qualifiedByName = "priorityToDisplay")
    @Mapping(target = "rfqTypeDisplay", source = "rfqType", qualifiedByName = "rfqTypeToDisplay")
    @Mapping(target = "cargoTypeDisplay", source = "cargoType", qualifiedByName = "cargoTypeToDisplay")
    @Mapping(target = "truckTypeDisplay", source = "truckType", qualifiedByName = "truckTypeToDisplay")
    @Mapping(target = "transportModeDisplay", source = "transportMode", qualifiedByName = "transportModeToDisplay")
    @Mapping(target = "pricingModelDisplay", source = "pricingModel", qualifiedByName = "pricingModelToDisplay")
    @Mapping(target = "providerTypeDisplay", source = "providerType", qualifiedByName = "providerTypeToDisplay")
    @Mapping(target = "childRfqCount", ignore = true)
    @Mapping(target = "parentRfqCount", ignore = true)
    LogisticsRFQDto toDto(LogisticsRFQ entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rfqNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    LogisticsRFQ toEntity(LogisticsRFQDto dto);

    List<LogisticsRFQDto> toDtoList(List<LogisticsRFQ> entities);

    List<LogisticsRFQ> toEntityList(List<LogisticsRFQDto> dtos);

    @Named("statusToDisplay")
    default String statusToDisplay(LogisticsRFQ.RFQStatus status) {
        if (status == null)
            return null;
        return status.name().replace("_", " ");
    }

    @Named("priorityToDisplay")
    default String priorityToDisplay(LogisticsRFQ.Priority priority) {
        if (priority == null)
            return null;
        return priority.name().replace("_", " ");
    }

    @Named("rfqTypeToDisplay")
    default String rfqTypeToDisplay(LogisticsRFQ.RFQType rfqType) {
        if (rfqType == null)
            return null;
        return rfqType.name().replace("_", " ");
    }

    @Named("cargoTypeToDisplay")
    default String cargoTypeToDisplay(LogisticsRFQ.CargoType cargoType) {
        if (cargoType == null)
            return null;
        return cargoType.name().replace("_", " ");
    }

    @Named("truckTypeToDisplay")
    default String truckTypeToDisplay(LogisticsRFQ.TruckType truckType) {
        if (truckType == null)
            return null;
        return truckType.name().replace("_", " ");
    }

    @Named("transportModeToDisplay")
    default String transportModeToDisplay(LogisticsRFQ.TransportMode transportMode) {
        if (transportMode == null)
            return null;
        return transportMode.name().replace("_", " ");
    }

    @Named("pricingModelToDisplay")
    default String pricingModelToDisplay(LogisticsRFQ.PricingModel pricingModel) {
        if (pricingModel == null)
            return null;
        return pricingModel.name().replace("_", " ");
    }

    @Named("providerTypeToDisplay")
    default String providerTypeToDisplay(LogisticsRFQ.ProviderType providerType) {
        if (providerType == null)
            return null;
        return providerType.name().replace("_", " ");
    }
}