package com.notificationservice.dto;

import lombok.Data;

@Data
public class FileUploadRequestDto {
    private String fileName;
    private String displayName;
    private Long parentId;
    private String description;
    private Boolean isPublic;
    private String tags;
    private String metadata;
}