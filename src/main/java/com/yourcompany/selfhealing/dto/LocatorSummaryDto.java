package com.yourcompany.selfhealing.dto;

import java.time.LocalDateTime;

public record LocatorSummaryDto(
        String locatorName,
        String pageUrl,
        Integer healCount,
        Integer healSuccessCount,
        Integer healFailureCount,
        Double averageValidationScore,
        LocalDateTime lastHealedAt,
        LocalDateTime lastValidatedAt
) {}
