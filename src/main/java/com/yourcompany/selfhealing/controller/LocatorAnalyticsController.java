package com.yourcompany.selfhealing.controller;

import com.yourcompany.selfhealing.dto.HealingHistoryRecordDto;
import com.yourcompany.selfhealing.dto.LocatorDetailDto;
import com.yourcompany.selfhealing.dto.LocatorSummaryDto;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/healing/locators")
public class LocatorAnalyticsController {

    private final LocatorMetaService locatorMetaService;

    public LocatorAnalyticsController(LocatorMetaService locatorMetaService) {
        this.locatorMetaService = locatorMetaService;
    }

    @GetMapping
    public List<LocatorSummaryDto> listLocators(@RequestParam(name = "minHealCount", required = false) Integer minHealCount) {
        if (minHealCount != null) {
            return locatorMetaService.getFrequentlyHealed(minHealCount);
        }
        return locatorMetaService.getLocatorSummaries();
    }

    @GetMapping("/{locatorName}")
    public ResponseEntity<LocatorDetailDto> getLocatorDetail(@PathVariable String locatorName) {
        return locatorMetaService
                .getLocatorDetail(locatorName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{locatorName}/history")
    public List<HealingHistoryRecordDto> getLocatorHistory(
            @PathVariable String locatorName,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return locatorMetaService.getHealingHistory(locatorName, limit);
    }
}
