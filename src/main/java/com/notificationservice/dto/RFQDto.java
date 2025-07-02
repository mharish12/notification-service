package com.notificationservice.dto;

import com.notificationservice.entity.RFQ;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RFQDto {
    private Long id;

    @NotBlank(message = "Reference number is required")
    @Size(max = 100, message = "Reference number must be at most 100 characters")
    private String referenceNumber;

    @Size(max = 255, message = "Title must be at most 255 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "State is required")
    private RFQ.State state;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}