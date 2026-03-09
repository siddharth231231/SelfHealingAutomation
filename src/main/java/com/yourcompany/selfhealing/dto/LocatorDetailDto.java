package com.yourcompany.selfhealing.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LocatorDetailDto(
        String locatorName,
        String pageUrl,
        String pageTitle,
        String currentActiveLocator,
        Integer locatorVersion,
        Integer healCount,
        Integer healSuccessCount,
        Integer healFailureCount,
        Double averageValidationScore,
        Double locatorConfidence,
        Double ambiguityScore,
        String dataTestId,
        LocalDateTime lastHealedAt,
        LocalDateTime lastValidatedAt,
        List<HealingHistoryRecordDto> healingHistory
) {}
