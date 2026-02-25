package com.yourcompany.selfhealing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "locator_metadata",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"locator_name", "page_url"}
        )
)
public class LocatorMetaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // Locator Identity
    // ===============================

    @Column(name = "locator_name", nullable = false)
    private String locatorName;

    @Column(name = "original_locator", columnDefinition = "TEXT", nullable = false)
    private String originalLocator;

    @Column(name = "current_active_locator", columnDefinition = "TEXT")
    private String currentActiveLocator;

    // ===============================
    // Multiple Locator Anchors
    // ===============================

    @Column(name = "relative_xpath", columnDefinition = "TEXT")
    private String relativeXpath;

    @Column(name = "absolute_xpath", columnDefinition = "TEXT")
    private String absoluteXpath;

    @Column(name = "css_selector", columnDefinition = "TEXT")
    private String cssSelector;

    @Column(name = "parent_xpath_chain", columnDefinition = "LONGTEXT")
    private String parentXpathChain;

    @Column(name = "sibling_xpath_cluster", columnDefinition = "LONGTEXT")
    private String siblingXpathCluster;

    // ===============================
    // Page Context
    // ===============================

    @Column(name = "page_url", columnDefinition = "TEXT")
    private String pageUrl;

    @Column(name = "page_title")
    private String pageTitle;

    // ===============================
    // DOM Snapshot (Healenium Style)
    // ===============================

    @Column(name = "dom_snapshot", columnDefinition = "LONGTEXT")
    private String domSnapshot;

    @Column(name = "dom_hash", length = 255)
    private String domHash;

    // ===============================
    // Healing Metadata
    // ===============================

    @Column(name = "locator_version")
    private Integer locatorVersion = 1;

    @Column(name = "heal_count")
    private Integer healCount = 0;

    @Column(name = "last_similarity_score")
    private Double lastSimilarityScore;

    // ===============================
    // Audit Fields
    // ===============================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===============================
    // Lifecycle Hooks
    // ===============================

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===============================
    // Getters & Setters
    // ===============================

    public Long getId() { return id; }

    public String getLocatorName() { return locatorName; }
    public void setLocatorName(String locatorName) { this.locatorName = locatorName; }

    public String getOriginalLocator() { return originalLocator; }
    public void setOriginalLocator(String originalLocator) { this.originalLocator = originalLocator; }

    public String getCurrentActiveLocator() { return currentActiveLocator; }
    public void setCurrentActiveLocator(String currentActiveLocator) { this.currentActiveLocator = currentActiveLocator; }

    public String getRelativeXpath() { return relativeXpath; }
    public void setRelativeXpath(String relativeXpath) { this.relativeXpath = relativeXpath; }

    public String getAbsoluteXpath() { return absoluteXpath; }
    public void setAbsoluteXpath(String absoluteXpath) { this.absoluteXpath = absoluteXpath; }

    public String getCssSelector() { return cssSelector; }
    public void setCssSelector(String cssSelector) { this.cssSelector = cssSelector; }

    public String getParentXpathChain() { return parentXpathChain; }
    public void setParentXpathChain(String parentXpathChain) { this.parentXpathChain = parentXpathChain; }

    public String getSiblingXpathCluster() { return siblingXpathCluster; }
    public void setSiblingXpathCluster(String siblingXpathCluster) { this.siblingXpathCluster = siblingXpathCluster; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public String getDomSnapshot() { return domSnapshot; }
    public void setDomSnapshot(String domSnapshot) { this.domSnapshot = domSnapshot; }

    public String getDomHash() { return domHash; }
    public void setDomHash(String domHash) { this.domHash = domHash; }

    public Integer getLocatorVersion() { return locatorVersion; }
    public void setLocatorVersion(Integer locatorVersion) { this.locatorVersion = locatorVersion; }

    public Integer getHealCount() { return healCount; }
    public void setHealCount(Integer healCount) { this.healCount = healCount; }

    public Double getLastSimilarityScore() { return lastSimilarityScore; }
    public void setLastSimilarityScore(Double lastSimilarityScore) { this.lastSimilarityScore = lastSimilarityScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}