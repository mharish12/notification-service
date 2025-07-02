package com.notificationservice.dto.validation;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationSummaryDto {

    private long totalValidations;
    private long validCount;
    private long invalidCount;
    private long totalViolations;
    private double successRate;
    private LocalDateTime summaryTime;

    public ValidationSummaryDto(long totalValidations, long validCount, long invalidCount, long totalViolations,
            double successRate) {
        this.totalValidations = totalValidations;
        this.validCount = validCount;
        this.invalidCount = invalidCount;
        this.totalViolations = totalViolations;
        this.successRate = successRate;
        this.summaryTime = LocalDateTime.now();
    }

    public double getFailureRate() {
        return 1.0 - successRate;
    }

    public boolean isAllValid() {
        return invalidCount == 0;
    }

    public boolean hasAnyInvalidation() {
        return invalidCount > 0;
    }
}