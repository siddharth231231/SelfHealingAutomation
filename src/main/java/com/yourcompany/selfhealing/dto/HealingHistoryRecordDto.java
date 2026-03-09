package com.yourcompany.selfhealing.dto;

import java.time.LocalDateTime;

public record HealingHistoryRecordDto(
        Integer attemptNumber,
        String status,
        String failureReason,
        String selectedXpath,
        String healeniumXpath,
        Double healeniumScore,
        LocalDateTime createdAt
) {}
