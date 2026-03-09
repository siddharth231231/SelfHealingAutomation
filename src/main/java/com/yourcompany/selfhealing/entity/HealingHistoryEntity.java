package com.yourcompany.selfhealing.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "healing_history",
        indexes = {
                @Index(name = "idx_hist_locator_name", columnList = "locator_name"),
                @Index(name = "idx_hist_created_at", columnList = "created_at")
        }
)
public class HealingHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "locator_name", nullable = false)
    private String locatorName;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "ai_suggestions_json", columnDefinition = "LONGTEXT")
    private String aiSuggestionsJson;

    @Column(name = "selected_xpath", columnDefinition = "TEXT")
    private String selectedXpath;

    @Column(name = "healenium_xpath", columnDefinition = "TEXT")
    private String healeniumXpath;

    @Column(name = "healenium_score")
    private Double healeniumScore;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getLocatorName() { return locatorName; }
    public void setLocatorName(String locatorName) { this.locatorName = locatorName; }
    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }
    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getAiSuggestionsJson() { return aiSuggestionsJson; }
    public void setAiSuggestionsJson(String aiSuggestionsJson) { this.aiSuggestionsJson = aiSuggestionsJson; }
    public String getSelectedXpath() { return selectedXpath; }
    public void setSelectedXpath(String selectedXpath) { this.selectedXpath = selectedXpath; }
    public String getHealeniumXpath() { return healeniumXpath; }
    public void setHealeniumXpath(String healeniumXpath) { this.healeniumXpath = healeniumXpath; }
    public Double getHealeniumScore() { return healeniumScore; }
    public void setHealeniumScore(Double healeniumScore) { this.healeniumScore = healeniumScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
