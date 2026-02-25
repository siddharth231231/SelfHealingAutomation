package com.yourcompany.selfhealing.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "locator_metadata",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"locator_name", "page_url"}
        ),
        indexes = {
                @Index(name = "idx_locator_name", columnList = "locator_name"),
                @Index(name = "idx_page_url", columnList = "page_url"),
                @Index(name = "idx_dom_hash", columnList = "dom_hash"),
                @Index(name = "idx_heal_count", columnList = "heal_count"),
                @Index(name = "idx_updated_at", columnList = "updated_at")
        }
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

    @Column(name = "parent_xpath", columnDefinition = "TEXT")
    private String parentXpath;

    @Column(name = "sibling_xpaths", columnDefinition = "LONGTEXT")
    private String siblingXpaths;

    @Column(name = "parent_xpath_chain", columnDefinition = "LONGTEXT")
    private String parentXpathChain;

    @Column(name = "sibling_xpath_cluster", columnDefinition = "LONGTEXT")
    private String siblingXpathCluster;

    @Column(name = "element_tag", length = 100)
    private String elementTag;

    @Column(name = "element_role", length = 100)
    private String elementRole;

    @Column(name = "element_type", length = 100)
    private String elementType;

    @Column(name = "normalized_visible_text", columnDefinition = "TEXT")
    private String normalizedVisibleText;

    @Column(name = "stable_attribute_json", columnDefinition = "LONGTEXT")
    private String stableAttributeJson;

    @Column(name = "volatile_attribute_json", columnDefinition = "LONGTEXT")
    private String volatileAttributeJson;

    @Column(name = "anchor_hierarchy_json", columnDefinition = "LONGTEXT")
    private String anchorHierarchyJson;

    @Column(name = "sibling_signature_json", columnDefinition = "LONGTEXT")
    private String siblingSignatureJson;

    // ===============================
    // Page Context
    // ===============================

    @Column(name = "page_url", length = 1024)
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

    @Column(name = "average_validation_score")
    private Double averageValidationScore;

    @Column(name = "locator_confidence")
    private Double locatorConfidence;

    @Column(name = "ambiguity_score")
    private Double ambiguityScore;

    @Column(name = "heal_success_count")
    private Integer healSuccessCount = 0;

    @Column(name = "heal_failure_count")
    private Integer healFailureCount = 0;

    @Column(name = "last_healed_at")
    private LocalDateTime lastHealedAt;

    @Column(name = "last_validated_at")
    private LocalDateTime lastValidatedAt;

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

    public String getParentXpath() { return parentXpath; }
    public void setParentXpath(String parentXpath) { this.parentXpath = parentXpath; }

    public String getSiblingXpaths() { return siblingXpaths; }
    public void setSiblingXpaths(String siblingXpaths) { this.siblingXpaths = siblingXpaths; }

    public String getParentXpathChain() { return parentXpathChain; }
    public void setParentXpathChain(String parentXpathChain) { this.parentXpathChain = parentXpathChain; }

    public String getSiblingXpathCluster() { return siblingXpathCluster; }
    public void setSiblingXpathCluster(String siblingXpathCluster) { this.siblingXpathCluster = siblingXpathCluster; }

    public String getElementTag() { return elementTag; }
    public void setElementTag(String elementTag) { this.elementTag = elementTag; }

    public String getElementRole() { return elementRole; }
    public void setElementRole(String elementRole) { this.elementRole = elementRole; }

    public String getElementType() { return elementType; }
    public void setElementType(String elementType) { this.elementType = elementType; }

    public String getNormalizedVisibleText() { return normalizedVisibleText; }
    public void setNormalizedVisibleText(String normalizedVisibleText) { this.normalizedVisibleText = normalizedVisibleText; }

    public String getStableAttributeJson() { return stableAttributeJson; }
    public void setStableAttributeJson(String stableAttributeJson) { this.stableAttributeJson = stableAttributeJson; }

    public String getVolatileAttributeJson() { return volatileAttributeJson; }
    public void setVolatileAttributeJson(String volatileAttributeJson) { this.volatileAttributeJson = volatileAttributeJson; }

    public String getAnchorHierarchyJson() { return anchorHierarchyJson; }
    public void setAnchorHierarchyJson(String anchorHierarchyJson) { this.anchorHierarchyJson = anchorHierarchyJson; }

    public String getSiblingSignatureJson() { return siblingSignatureJson; }
    public void setSiblingSignatureJson(String siblingSignatureJson) { this.siblingSignatureJson = siblingSignatureJson; }

    // Backward-compatible aliases used by older test/context mappers.
    public String getParentDomSnapshot() { return parentXpathChain; }
    public void setParentDomSnapshot(String parentDomSnapshot) { this.parentXpathChain = parentDomSnapshot; }

    public String getSiblingsDomSnapshot() { return siblingXpathCluster; }
    public void setSiblingsDomSnapshot(String siblingsDomSnapshot) { this.siblingXpathCluster = siblingsDomSnapshot; }

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

    public Double getAverageValidationScore() { return averageValidationScore; }
    public void setAverageValidationScore(Double averageValidationScore) { this.averageValidationScore = averageValidationScore; }

    public Double getLocatorConfidence() { return locatorConfidence; }
    public void setLocatorConfidence(Double locatorConfidence) { this.locatorConfidence = locatorConfidence; }

    public Double getAmbiguityScore() { return ambiguityScore; }
    public void setAmbiguityScore(Double ambiguityScore) { this.ambiguityScore = ambiguityScore; }

    public Integer getHealSuccessCount() { return healSuccessCount; }
    public void setHealSuccessCount(Integer healSuccessCount) { this.healSuccessCount = healSuccessCount; }

    public Integer getHealFailureCount() { return healFailureCount; }
    public void setHealFailureCount(Integer healFailureCount) { this.healFailureCount = healFailureCount; }

    public LocalDateTime getLastHealedAt() { return lastHealedAt; }
    public void setLastHealedAt(LocalDateTime lastHealedAt) { this.lastHealedAt = lastHealedAt; }

    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }
    public void setLastValidatedAt(LocalDateTime lastValidatedAt) { this.lastValidatedAt = lastValidatedAt; }


    @Override
    public String toString() {
        return "LocatorMetaEntity{" +
                "locatorVersion=" + locatorVersion +
                ", id=" + id +
                ", locatorName='" + locatorName + '\'' +
                ", originalLocator='" + originalLocator + '\'' +
                ", currentActiveLocator='" + currentActiveLocator + '\'' +
                ", relativeXpath='" + relativeXpath + '\'' +
                ", absoluteXpath='" + absoluteXpath + '\'' +
                ", cssSelector='" + cssSelector + '\'' +
                ", parentXpath='" + parentXpath + '\'' +
                ", siblingXpaths='" + siblingXpaths + '\'' +
                ", parentXpathChain='" + parentXpathChain + '\'' +
                ", siblingXpathCluster='" + siblingXpathCluster + '\'' +
                ", elementTag='" + elementTag + '\'' +
                ", elementRole='" + elementRole + '\'' +
                ", elementType='" + elementType + '\'' +
                ", normalizedVisibleText='" + normalizedVisibleText + '\'' +
                ", stableAttributeJson='" + stableAttributeJson + '\'' +
                ", volatileAttributeJson='" + volatileAttributeJson + '\'' +
                ", anchorHierarchyJson='" + anchorHierarchyJson + '\'' +
                ", siblingSignatureJson='" + siblingSignatureJson + '\'' +
                ", pageUrl='" + pageUrl + '\'' +
                ", pageTitle='" + pageTitle + '\'' +
                ", domSnapshot='" + domSnapshot + '\'' +
                ", domHash='" + domHash + '\'' +
                ", healCount=" + healCount +
                ", lastSimilarityScore=" + lastSimilarityScore +
                ", averageValidationScore=" + averageValidationScore +
                ", locatorConfidence=" + locatorConfidence +
                ", ambiguityScore=" + ambiguityScore +
                ", healSuccessCount=" + healSuccessCount +
                ", healFailureCount=" + healFailureCount +
                ", lastHealedAt=" + lastHealedAt +
                ", lastValidatedAt=" + lastValidatedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
