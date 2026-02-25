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

    private String pageUrl;
    private String pageTitle;

    private String domSnapshot;
    private String domHash;

    private Integer locatorVersion;
    private Integer healCount;
    private Double lastSimilarityScore;

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
        data.pageUrl = entity.getPageUrl();
        data.pageTitle = entity.getPageTitle();
        data.domSnapshot = entity.getDomSnapshot();
        data.domHash = entity.getDomHash();
        data.locatorVersion = entity.getLocatorVersion();
        data.healCount = entity.getHealCount();
        data.lastSimilarityScore = entity.getLastSimilarityScore();
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
    public String getPageUrl() { return pageUrl; }
    public String getPageTitle() { return pageTitle; }
    public String getDomSnapshot() { return domSnapshot; }
    public String getDomHash() { return domHash; }
    public Integer getLocatorVersion() { return locatorVersion; }
    public Integer getHealCount() { return healCount; }
    public Double getLastSimilarityScore() { return lastSimilarityScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}