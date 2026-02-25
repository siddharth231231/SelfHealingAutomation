package selfhealing.context.dom;

import java.util.List;
import java.util.Map;

/**
 * Rich DOM context for intelligent self-healing.
 * Combines stored metadata + live DOM intelligence.
 */
//hi
public class DomContext {

    /* ================= FAILURE / INTENT ================= */

    private String brokenXpath;
    private String normalizedBrokenXpath;
    private String expectedTag;
    private String expectedText;
    private Map<String, String> expectedAttributes;
    private Integer brokenXpathDepth;
    private Integer brokenXpathDynamicRiskScore;
    private List<String> brokenXpathRiskReasons;
    private String failureType;

    /* ================= PAGE CONTEXT ================= */

    private String pageUrl;
    private String pageTitle;
    private String pageRole;

    private boolean samePage;
    private boolean domChanged;
    private String currentDomHash;

    /* ================= MODE ================= */

    private boolean elementFound;

    /* ================= STRUCTURAL CONTEXT ================= */

    private boolean relativeXpathValid;
    private boolean absoluteXpathValid;
    private boolean cssSelectorValid;
    private boolean brokenXpathValid;
    private Integer brokenXpathMatchCount;

    private boolean parentStillExists;
    private boolean siblingClusterStillExists;

    private String semanticParentHtml;
    private List<String> siblingElements;

    private Integer parentChildCount;
    private Integer elementIndexInsideParent;

    /* ================= TEXT INTELLIGENCE ================= */

    private String closestMatchingText;
    private String nearbyText;
    private List<Map<String, Object>> candidateElements;
    private List<String> topCandidateXpaths;

    /* ================= ATTRIBUTE SIGNALS ================= */

    private Map<String, String> stableAttributes;
    private Map<String, String> liveAttributes;

    /* ================= LAYOUT INTELLIGENCE ================= */

    private String viewportSize;
    private String scrollPosition;
    private String elementCoordinates;   // x,y
    private String elementSize;          // width,height
    private Boolean elementVisible;
    private Boolean elementEnabled;

    /* ================= DOM METRICS ================= */

    private Integer fullDomLength;
    private String bodyTextSnippet;

    /* ================= STRATEGY CONSTRAINTS ================= */

    private List<String> avoidStrategies;
    private List<String> preferredStrategies;

    /* ========================================================= */
    /* ================= GETTERS / SETTERS ===================== */
    /* ========================================================= */

    public String getBrokenXpath() { return brokenXpath; }
    public void setBrokenXpath(String brokenXpath) { this.brokenXpath = brokenXpath; }

    public String getNormalizedBrokenXpath() { return normalizedBrokenXpath; }
    public void setNormalizedBrokenXpath(String normalizedBrokenXpath) { this.normalizedBrokenXpath = normalizedBrokenXpath; }

    public String getExpectedTag() { return expectedTag; }
    public void setExpectedTag(String expectedTag) { this.expectedTag = expectedTag; }

    public String getExpectedText() { return expectedText; }
    public void setExpectedText(String expectedText) { this.expectedText = expectedText; }

    public Map<String, String> getExpectedAttributes() { return expectedAttributes; }
    public void setExpectedAttributes(Map<String, String> expectedAttributes) { this.expectedAttributes = expectedAttributes; }

    public Integer getBrokenXpathDepth() { return brokenXpathDepth; }
    public void setBrokenXpathDepth(Integer brokenXpathDepth) { this.brokenXpathDepth = brokenXpathDepth; }

    public Integer getBrokenXpathDynamicRiskScore() { return brokenXpathDynamicRiskScore; }
    public void setBrokenXpathDynamicRiskScore(Integer brokenXpathDynamicRiskScore) {
        this.brokenXpathDynamicRiskScore = brokenXpathDynamicRiskScore;
    }

    public List<String> getBrokenXpathRiskReasons() { return brokenXpathRiskReasons; }
    public void setBrokenXpathRiskReasons(List<String> brokenXpathRiskReasons) {
        this.brokenXpathRiskReasons = brokenXpathRiskReasons;
    }

    public String getFailureType() { return failureType; }
    public void setFailureType(String failureType) { this.failureType = failureType; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public String getPageRole() { return pageRole; }
    public void setPageRole(String pageRole) { this.pageRole = pageRole; }

    public boolean isSamePage() { return samePage; }
    public void setSamePage(boolean samePage) { this.samePage = samePage; }

    public boolean isDomChanged() { return domChanged; }
    public void setDomChanged(boolean domChanged) { this.domChanged = domChanged; }

    public String getCurrentDomHash() { return currentDomHash; }
    public void setCurrentDomHash(String currentDomHash) { this.currentDomHash = currentDomHash; }

    public boolean isElementFound() { return elementFound; }
    public void setElementFound(boolean elementFound) { this.elementFound = elementFound; }

    public boolean isRelativeXpathValid() { return relativeXpathValid; }
    public void setRelativeXpathValid(boolean relativeXpathValid) { this.relativeXpathValid = relativeXpathValid; }

    public boolean isAbsoluteXpathValid() { return absoluteXpathValid; }
    public void setAbsoluteXpathValid(boolean absoluteXpathValid) { this.absoluteXpathValid = absoluteXpathValid; }

    public boolean isCssSelectorValid() { return cssSelectorValid; }
    public void setCssSelectorValid(boolean cssSelectorValid) { this.cssSelectorValid = cssSelectorValid; }

    public boolean isBrokenXpathValid() { return brokenXpathValid; }
    public void setBrokenXpathValid(boolean brokenXpathValid) { this.brokenXpathValid = brokenXpathValid; }

    public Integer getBrokenXpathMatchCount() { return brokenXpathMatchCount; }
    public void setBrokenXpathMatchCount(Integer brokenXpathMatchCount) { this.brokenXpathMatchCount = brokenXpathMatchCount; }

    public boolean isParentStillExists() { return parentStillExists; }
    public void setParentStillExists(boolean parentStillExists) { this.parentStillExists = parentStillExists; }

    public boolean isSiblingClusterStillExists() { return siblingClusterStillExists; }
    public void setSiblingClusterStillExists(boolean siblingClusterStillExists) { this.siblingClusterStillExists = siblingClusterStillExists; }

    public String getSemanticParentHtml() { return semanticParentHtml; }
    public void setSemanticParentHtml(String semanticParentHtml) { this.semanticParentHtml = semanticParentHtml; }

    public List<String> getSiblingElements() { return siblingElements; }
    public void setSiblingElements(List<String> siblingElements) { this.siblingElements = siblingElements; }

    public Integer getParentChildCount() { return parentChildCount; }
    public void setParentChildCount(Integer parentChildCount) { this.parentChildCount = parentChildCount; }

    public Integer getElementIndexInsideParent() { return elementIndexInsideParent; }
    public void setElementIndexInsideParent(Integer elementIndexInsideParent) {
        this.elementIndexInsideParent = elementIndexInsideParent;
    }

    public String getClosestMatchingText() { return closestMatchingText; }
    public void setClosestMatchingText(String closestMatchingText) {
        this.closestMatchingText = closestMatchingText;
    }

    public String getNearbyText() { return nearbyText; }
    public void setNearbyText(String nearbyText) { this.nearbyText = nearbyText; }

    public List<Map<String, Object>> getCandidateElements() { return candidateElements; }
    public void setCandidateElements(List<Map<String, Object>> candidateElements) {
        this.candidateElements = candidateElements;
    }

    public List<String> getTopCandidateXpaths() { return topCandidateXpaths; }
    public void setTopCandidateXpaths(List<String> topCandidateXpaths) { this.topCandidateXpaths = topCandidateXpaths; }

    public Map<String, String> getStableAttributes() { return stableAttributes; }
    public void setStableAttributes(Map<String, String> stableAttributes) {
        this.stableAttributes = stableAttributes;
    }

    public Map<String, String> getLiveAttributes() { return liveAttributes; }
    public void setLiveAttributes(Map<String, String> liveAttributes) {
        this.liveAttributes = liveAttributes;
    }

    public String getViewportSize() { return viewportSize; }
    public void setViewportSize(String viewportSize) { this.viewportSize = viewportSize; }

    public String getScrollPosition() { return scrollPosition; }
    public void setScrollPosition(String scrollPosition) { this.scrollPosition = scrollPosition; }

    public String getElementCoordinates() { return elementCoordinates; }
    public void setElementCoordinates(String elementCoordinates) {
        this.elementCoordinates = elementCoordinates;
    }

    public String getElementSize() { return elementSize; }
    public void setElementSize(String elementSize) { this.elementSize = elementSize; }

    public Boolean getElementVisible() { return elementVisible; }
    public void setElementVisible(Boolean elementVisible) { this.elementVisible = elementVisible; }

    public Boolean getElementEnabled() { return elementEnabled; }
    public void setElementEnabled(Boolean elementEnabled) { this.elementEnabled = elementEnabled; }

    public Integer getFullDomLength() { return fullDomLength; }
    public void setFullDomLength(Integer fullDomLength) { this.fullDomLength = fullDomLength; }

    public String getBodyTextSnippet() { return bodyTextSnippet; }
    public void setBodyTextSnippet(String bodyTextSnippet) { this.bodyTextSnippet = bodyTextSnippet; }

    public List<String> getAvoidStrategies() { return avoidStrategies; }
    public void setAvoidStrategies(List<String> avoidStrategies) {
        this.avoidStrategies = avoidStrategies;
    }

    public List<String> getPreferredStrategies() { return preferredStrategies; }
    public void setPreferredStrategies(List<String> preferredStrategies) {
        this.preferredStrategies = preferredStrategies;
    }

    /* ================= STRING DEBUG OUTPUT ================= */

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("\n========== RICH SELF-HEALING DOM CONTEXT ==========\n");

        sb.append("\n--- Intent ---\n");
        sb.append("Broken XPath   : ").append(brokenXpath).append("\n");
        sb.append("Normalized     : ").append(normalizedBrokenXpath).append("\n");
        sb.append("Expected Tag   : ").append(expectedTag).append("\n");
        sb.append("Expected Text  : ").append(expectedText).append("\n");
        sb.append("XPath Depth    : ").append(brokenXpathDepth).append("\n");
        sb.append("Dynamic Risk   : ").append(brokenXpathDynamicRiskScore).append("\n");
        sb.append("Failure Type   : ").append(failureType).append("\n");

        sb.append("\n--- Page Info ---\n");
        sb.append("URL            : ").append(pageUrl).append("\n");
        sb.append("Title          : ").append(pageTitle).append("\n");
        sb.append("Same Page      : ").append(samePage).append("\n");
        sb.append("DOM Changed    : ").append(domChanged).append("\n");
        sb.append("DOM Hash       : ").append(currentDomHash).append("\n");

        sb.append("\n--- Structural Signals ---\n");
        sb.append("Relative OK    : ").append(relativeXpathValid).append("\n");
        sb.append("Absolute OK    : ").append(absoluteXpathValid).append("\n");
        sb.append("CSS OK         : ").append(cssSelectorValid).append("\n");
        sb.append("Broken XPath OK: ").append(brokenXpathValid).append(" (")
                .append(brokenXpathMatchCount).append(" matches)\n");
        sb.append("Parent Exists  : ").append(parentStillExists).append("\n");
        sb.append("Sibling Exists : ").append(siblingClusterStillExists).append("\n");

        sb.append("\n--- Parent DOM ---\n");
        sb.append(truncate(semanticParentHtml)).append("\n");

        sb.append("\n--- Text Intelligence ---\n");
        sb.append("Closest Match  : ").append(closestMatchingText).append("\n");

        sb.append("\n--- Layout ---\n");
        sb.append("Viewport       : ").append(viewportSize).append("\n");
        sb.append("Scroll         : ").append(scrollPosition).append("\n");

        sb.append("\n====================================================\n");

        return sb.toString();
    }

    private String truncate(String html) {
        if (html == null) return "N/A";
        return html.length() > 1000
                ? html.substring(0, 1000) + " ...[truncated]"
                : html;
    }
}
