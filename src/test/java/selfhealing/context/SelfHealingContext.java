package selfhealing.context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified FLAT context object sent to Spring AI.
 * Contains runtime DOM data + historical DB metadata
 * in a single flat structure for easier JSON mapping.
 */
public class SelfHealingContext {

    /* =================================================
       Test Metadata
       ================================================= */
    private String testName;

    /* =================================================
       Runtime Failure Context (DomContext fields)
       ================================================= */

    private String brokenXpath;
    private String expectedTag;
    private String expectedText;
    private String failureType;

    private String pageUrl;
    private String pageTitle;
    private String pageRole;

    private boolean elementFound;

    private String semanticParentHtml;
    private List<String> siblingElements;

    private String closestMatchingText;
    private String nearbyText;

    private Map<String, String> stableAttributes;

    private List<String> avoidStrategies;
    private List<String> preferredStrategies;

    /* =================================================
       Historical Locator Metadata (DbExtractedData fields)
       ================================================= */

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

    private String historicalPageUrl;
    private String historicalPageTitle;

    private String domSnapshot;
    private String domHash;

    private Integer locatorVersion;
    private Integer healCount;
    private Double lastSimilarityScore;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /* =================================================
       Getters and Setters
       ================================================= */

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getBrokenXpath() { return brokenXpath; }
    public void setBrokenXpath(String brokenXpath) { this.brokenXpath = brokenXpath; }

    public String getExpectedTag() { return expectedTag; }
    public void setExpectedTag(String expectedTag) { this.expectedTag = expectedTag; }

    public String getExpectedText() { return expectedText; }
    public void setExpectedText(String expectedText) { this.expectedText = expectedText; }

    public String getFailureType() { return failureType; }
    public void setFailureType(String failureType) { this.failureType = failureType; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public String getPageRole() { return pageRole; }
    public void setPageRole(String pageRole) { this.pageRole = pageRole; }

    public boolean isElementFound() { return elementFound; }
    public void setElementFound(boolean elementFound) { this.elementFound = elementFound; }

    public String getSemanticParentHtml() { return semanticParentHtml; }
    public void setSemanticParentHtml(String semanticParentHtml) { this.semanticParentHtml = semanticParentHtml; }

    public List<String> getSiblingElements() { return siblingElements; }
    public void setSiblingElements(List<String> siblingElements) { this.siblingElements = siblingElements; }

    public String getClosestMatchingText() { return closestMatchingText; }
    public void setClosestMatchingText(String closestMatchingText) { this.closestMatchingText = closestMatchingText; }

    public String getNearbyText() { return nearbyText; }
    public void setNearbyText(String nearbyText) { this.nearbyText = nearbyText; }

    public Map<String, String> getStableAttributes() { return stableAttributes; }
    public void setStableAttributes(Map<String, String> stableAttributes) { this.stableAttributes = stableAttributes; }

    public List<String> getAvoidStrategies() { return avoidStrategies; }
    public void setAvoidStrategies(List<String> avoidStrategies) { this.avoidStrategies = avoidStrategies; }

    public List<String> getPreferredStrategies() { return preferredStrategies; }
    public void setPreferredStrategies(List<String> preferredStrategies) { this.preferredStrategies = preferredStrategies; }

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

    public String getHistoricalPageUrl() { return historicalPageUrl; }
    public void setHistoricalPageUrl(String historicalPageUrl) { this.historicalPageUrl = historicalPageUrl; }

    public String getHistoricalPageTitle() { return historicalPageTitle; }
    public void setHistoricalPageTitle(String historicalPageTitle) { this.historicalPageTitle = historicalPageTitle; }

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
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}