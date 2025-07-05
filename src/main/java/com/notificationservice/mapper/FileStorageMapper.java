package com.notificationservice.mapper;

import com.notificationservice.dto.FileStorageDto;
import com.notificationservice.entity.FileStorage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FileStorageMapper {

    @Mapping(target = "parentPath", ignore = true)
    @Mapping(target = "ownerName", ignore = true)
    @Mapping(target = "canEdit", ignore = true)
    @Mapping(target = "canDelete", ignore = true)
    @Mapping(target = "canShare", ignore = true)
    @Mapping(target = "childCount", ignore = true)
    @Mapping(target = "shareLink", ignore = true)
    FileStorageDto toDto(FileStorage entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    FileStorage toEntity(FileStorageDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(@MappingTarget FileStorage entity, FileStorageDto dto);

    List<FileStorageDto> toDtoList(List<FileStorage> entities);
}