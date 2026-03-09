package com.yourcompany.selfhealing.controller;

import com.yourcompany.selfhealing.dto.HealingHistoryRecordDto;
import com.yourcompany.selfhealing.dto.LocatorDetailDto;
import com.yourcompany.selfhealing.dto.LocatorSummaryDto;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocatorAnalyticsController.class)
class LocatorAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocatorMetaService locatorMetaService;

    @Test
    void listLocatorsReturnsSummary() throws Exception {
        LocatorSummaryDto summary = new LocatorSummaryDto(
                "btnLogin",
                "https://example.com/login",
                3,
                2,
                1,
                82.0,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(locatorMetaService.getLocatorSummaries()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/healing/locators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locatorName").value("btnLogin"));

        verify(locatorMetaService).getLocatorSummaries();
    }

    @Test
    void listLocatorsHonorsMinHealCount() throws Exception {
        when(locatorMetaService.getFrequentlyHealed(5)).thenReturn(List.of());

        mockMvc.perform(get("/api/healing/locators").param("minHealCount", "5"))
                .andExpect(status().isOk());

        verify(locatorMetaService).getFrequentlyHealed(5);
    }

    @Test
    void getLocatorDetailReturnsNotFoundIfMissing() throws Exception {
        when(locatorMetaService.getLocatorDetail("missing")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/healing/locators/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLocatorDetailReturnsData() throws Exception {
        HealingHistoryRecordDto history = new HealingHistoryRecordDto(
                1, "SUCCESS", null, "//button", null, null, LocalDateTime.now()
        );
        LocatorDetailDto detail = new LocatorDetailDto(
                "btnLogin",
                "https://example.com/login",
                "Login Page",
                "//button",
                2,
                3,
                2,
                1,
                85.0,
                0.76,
                0.22,
                "loginButton",
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(history)
        );
        when(locatorMetaService.getLocatorDetail("btnLogin")).thenReturn(Optional.of(detail));

        mockMvc.perform(get("/api/healing/locators/btnLogin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locatorName").value("btnLogin"))
                .andExpect(jsonPath("$.healingHistory[0].status").value("SUCCESS"));
    }

    @Test
    void getLocatorHistoryUsesLimit() throws Exception {
        when(locatorMetaService.getHealingHistory("btnLogin", 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/healing/locators/btnLogin/history").param("limit", "5"))
                .andExpect(status().isOk());

        verify(locatorMetaService).getHealingHistory("btnLogin", 5);
    }
}
