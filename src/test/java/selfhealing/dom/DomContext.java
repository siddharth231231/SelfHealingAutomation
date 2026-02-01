package selfhealing.dom;

import java.util.List;
import java.util.Map;

/**
 * Minimal, semantic DOM context for self-healing via LLM.
 * Designed to work even in worst-case DOM failures.
 */
public class DomContext {

    /* -------- Failure / Intent -------- */
    private String brokenXpath;
    private String expectedTag;
    private String expectedText;
    private String failureType;

    /* -------- Page Context -------- */
    private String pageUrl;
    private String pageTitle;
    private String pageRole; // NAVIGATION | FORM | CONTENT

    /* -------- Mode -------- */
    private boolean elementFound;

    /* -------- Structural Context -------- */
    private String semanticParentHtml;      // nav / header / section
    private List<String> siblingElements;   // same-tag siblings

    /* -------- Text Intelligence -------- */
    private String closestMatchingText;
    private String nearbyText;

    /* -------- Attribute Signals -------- */
    private Map<String, String> stableAttributes;

    /* -------- Constraints -------- */
    private List<String> avoidStrategies;
    private List<String> preferredStrategies;

    /* ================= Getters / Setters ================= */

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
    public void setSemanticParentHtml(String semanticParentHtml) {
        this.semanticParentHtml = semanticParentHtml;
    }

    public List<String> getSiblingElements() { return siblingElements; }
    public void setSiblingElements(List<String> siblingElements) {
        this.siblingElements = siblingElements;
    }

    public String getClosestMatchingText() { return closestMatchingText; }
    public void setClosestMatchingText(String closestMatchingText) {
        this.closestMatchingText = closestMatchingText;
    }

    public String getNearbyText() { return nearbyText; }
    public void setNearbyText(String nearbyText) { this.nearbyText = nearbyText; }

    public Map<String, String> getStableAttributes() { return stableAttributes; }
    public void setStableAttributes(Map<String, String> stableAttributes) {
        this.stableAttributes = stableAttributes;
    }

    public List<String> getAvoidStrategies() { return avoidStrategies; }
    public void setAvoidStrategies(List<String> avoidStrategies) {
        this.avoidStrategies = avoidStrategies;
    }

    public List<String> getPreferredStrategies() { return preferredStrategies; }
    public void setPreferredStrategies(List<String> preferredStrategies) {
        this.preferredStrategies = preferredStrategies;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n========== SELF-HEALING DOM CONTEXT ==========\n");

        sb.append("\n--- Intent ---\n");
        sb.append("Broken XPath    : ").append(brokenXpath).append("\n");
        sb.append("Expected Tag    : ").append(expectedTag).append("\n");
        sb.append("Expected Text   : ").append(expectedText).append("\n");
        sb.append("Failure Type    : ").append(failureType).append("\n");

        sb.append("\n--- Page Info ---\n");
        sb.append("URL             : ").append(pageUrl).append("\n");
        sb.append("Title           : ").append(pageTitle).append("\n");
        sb.append("Role            : ").append(pageRole).append("\n");

        if (!elementFound) {
            sb.append("\n⚠ ELEMENT NOT FOUND — SEARCH MODE ENABLED\n");
        }

        sb.append("\n--- Semantic Parent DOM ---\n");
        sb.append(truncate(semanticParentHtml)).append("\n");

        sb.append("\n--- Sibling Elements (Same Tag) ---\n");
        if (siblingElements != null && !siblingElements.isEmpty()) {
            siblingElements.forEach(s -> sb.append("  ").append(s).append("\n"));
        } else {
            sb.append("NONE\n");
        }

        sb.append("\n--- Text Intelligence ---\n");
        sb.append("Closest Match   : ").append(closestMatchingText).append("\n");

        sb.append("\n--- Nearby Text ---\n");
        sb.append(nearbyText != null ? nearbyText : "N/A").append("\n");

        sb.append("\n--- Constraints ---\n");
        sb.append("Avoid           : ").append(avoidStrategies).append("\n");
        sb.append("Prefer          : ").append(preferredStrategies).append("\n");

        sb.append("\n=============================================\n");

        return sb.toString();
    }

    private String truncate(String html) {
        if (html == null) return "N/A";
        return html.length() > 800
                ? html.substring(0, 800) + " ...[truncated]"
                : html;
    }

}
