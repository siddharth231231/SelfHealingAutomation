package selfhealing.context.storedContext;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;

import java.time.LocalDateTime;

/**
 * Simple DTO to hold DB extracted locator metadata
 * and pass it from BasePage to Listener.
 */
public class DbExtractedData {

    private String locatorName;
    private String originalLocator;
    private String currentActiveLocator;

    private String relativeXpath;
    private String absoluteXpath;
    private String cssSelector;

    private String parentXpath;
    private String siblingXpaths;

    private String parentXpathChain;
    private String siblingXpathCluster;

    private String parentDomSnapshot;
    private String siblingsDomSnapshot;

    private String elementTag;
    private String elementRole;
    private String elementType;
    private String normalizedVisibleText;
    private String stableAttributeJson;
    private String volatileAttributeJson;
    private String anchorHierarchyJson;
    private String siblingSignatureJson;

    private String pageUrl;
    private String pageTitle;

    private String domSnapshot;
    private String domHash;

    private Integer locatorVersion;
    private Integer healCount;
    private Double lastSimilarityScore;
    private Double averageValidationScore;
    private Double locatorConfidence;
    private Double ambiguityScore;
    private Integer healSuccessCount;
    private Integer healFailureCount;
    private LocalDateTime lastHealedAt;
    private LocalDateTime lastValidatedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ===============================
    // Static shared instance
    // ===============================

    private static DbExtractedData instance;

    public static void set(DbExtractedData data) {
        instance = data;
    }

    public static DbExtractedData get() {
        return instance;
    }

    public static void clear() {
        instance = null;
    }

    // ===============================
    // Helper method to map Entity → DTO
    // ===============================

    public static DbExtractedData fromEntity(LocatorMetaEntity entity) {

        DbExtractedData data = new DbExtractedData();

        data.locatorName = entity.getLocatorName();
        data.originalLocator = entity.getOriginalLocator();
        data.currentActiveLocator = entity.getCurrentActiveLocator();
        data.relativeXpath = entity.getRelativeXpath();
        data.absoluteXpath = entity.getAbsoluteXpath();
        data.cssSelector = entity.getCssSelector();
        data.parentXpath = entity.getParentXpath();
        data.siblingXpaths = entity.getSiblingXpaths();
        data.parentXpathChain = entity.getParentXpathChain();
        data.siblingXpathCluster = entity.getSiblingXpathCluster();
        data.parentDomSnapshot = entity.getParentDomSnapshot();
        data.siblingsDomSnapshot = entity.getSiblingsDomSnapshot();
        data.elementTag = entity.getElementTag();
        data.elementRole = entity.getElementRole();
        data.elementType = entity.getElementType();
        data.normalizedVisibleText = entity.getNormalizedVisibleText();
        data.stableAttributeJson = entity.getStableAttributeJson();
        data.volatileAttributeJson = entity.getVolatileAttributeJson();
        data.anchorHierarchyJson = entity.getAnchorHierarchyJson();
        data.siblingSignatureJson = entity.getSiblingSignatureJson();
        data.pageUrl = entity.getPageUrl();
        data.pageTitle = entity.getPageTitle();
        data.domSnapshot = entity.getDomSnapshot();
        data.domHash = entity.getDomHash();
        data.locatorVersion = entity.getLocatorVersion();
        data.healCount = entity.getHealCount();
        data.lastSimilarityScore = entity.getLastSimilarityScore();
        data.averageValidationScore = entity.getAverageValidationScore();
        data.locatorConfidence = entity.getLocatorConfidence();
        data.ambiguityScore = entity.getAmbiguityScore();
        data.healSuccessCount = entity.getHealSuccessCount();
        data.healFailureCount = entity.getHealFailureCount();
        data.lastHealedAt = entity.getLastHealedAt();
        data.lastValidatedAt = entity.getLastValidatedAt();
        data.createdAt = entity.getCreatedAt();
        data.updatedAt = entity.getUpdatedAt();

        return data;
    }

    // ===============================
    // Getters
    // ===============================

    public String getLocatorName() { return locatorName; }
    public String getOriginalLocator() { return originalLocator; }
    public String getCurrentActiveLocator() { return currentActiveLocator; }
    public String getRelativeXpath() { return relativeXpath; }
    public String getAbsoluteXpath() { return absoluteXpath; }
    public String getCssSelector() { return cssSelector; }
    public String getParentXpath() { return parentXpath; }
    public String getSiblingXpaths() { return siblingXpaths; }
    public String getParentXpathChain() { return parentXpathChain; }
    public String getSiblingXpathCluster() { return siblingXpathCluster; }
    public String getParentDomSnapshot() { return parentDomSnapshot; }
    public String getSiblingsDomSnapshot() { return siblingsDomSnapshot; }
    public String getElementTag() { return elementTag; }
    public String getElementRole() { return elementRole; }
    public String getElementType() { return elementType; }
    public String getNormalizedVisibleText() { return normalizedVisibleText; }
    public String getStableAttributeJson() { return stableAttributeJson; }
    public String getVolatileAttributeJson() { return volatileAttributeJson; }
    public String getAnchorHierarchyJson() { return anchorHierarchyJson; }
    public String getSiblingSignatureJson() { return siblingSignatureJson; }
    public String getPageUrl() { return pageUrl; }
    public String getPageTitle() { return pageTitle; }
    public String getDomSnapshot() { return domSnapshot; }
    public String getDomHash() { return domHash; }
    public Integer getLocatorVersion() { return locatorVersion; }
    public Integer getHealCount() { return healCount; }
    public Double getLastSimilarityScore() { return lastSimilarityScore; }
    public Double getAverageValidationScore() { return averageValidationScore; }
    public Double getLocatorConfidence() { return locatorConfidence; }
    public Double getAmbiguityScore() { return ambiguityScore; }
    public Integer getHealSuccessCount() { return healSuccessCount; }
    public Integer getHealFailureCount() { return healFailureCount; }
    public LocalDateTime getLastHealedAt() { return lastHealedAt; }
    public LocalDateTime getLastValidatedAt() { return lastValidatedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
