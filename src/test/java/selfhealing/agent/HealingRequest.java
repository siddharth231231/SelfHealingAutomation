package selfhealing.agent;

public class HealingRequest {

    private String brokenXpath;
    private String expectedTag;
    private String expectedText;

    private String locatorName;
    private String pageUrl;
    private int attempt;
    private int maxAttempts;

    private String storedElementFingerprintJson;
    private String storedStructuralFingerprintJson;
    private String storedNodePathJson;
    private String storedDomHash;

    private String liveAnchor;
    private String liveDomHash;
    private String liveDomRelevantHtml;

    private String healeniumSuggestedXpath;
    private Double healeniumScore;
    private String healeniumHintXpath; // explicit hint for AI ranking

    public String getBrokenXpath() {
        return brokenXpath;
    }

    public void setBrokenXpath(String brokenXpath) {
        this.brokenXpath = brokenXpath;
    }

    public String getExpectedTag() {
        return expectedTag;
    }

    public void setExpectedTag(String expectedTag) {
        this.expectedTag = expectedTag;
    }

    public String getExpectedText() {
        return expectedText;
    }

    public void setExpectedText(String expectedText) {
        this.expectedText = expectedText;
    }

    public String getLocatorName() {
        return locatorName;
    }

    public void setLocatorName(String locatorName) {
        this.locatorName = locatorName;
    }

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public int getAttempt() {
        return attempt;
    }

    public void setAttempt(int attempt) {
        this.attempt = attempt;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public String getStoredElementFingerprintJson() {
        return storedElementFingerprintJson;
    }

    public void setStoredElementFingerprintJson(String storedElementFingerprintJson) {
        this.storedElementFingerprintJson = storedElementFingerprintJson;
    }

    public String getStoredStructuralFingerprintJson() {
        return storedStructuralFingerprintJson;
    }

    public void setStoredStructuralFingerprintJson(String storedStructuralFingerprintJson) {
        this.storedStructuralFingerprintJson = storedStructuralFingerprintJson;
    }

    public String getStoredNodePathJson() {
        return storedNodePathJson;
    }

    public void setStoredNodePathJson(String storedNodePathJson) {
        this.storedNodePathJson = storedNodePathJson;
    }

    public String getStoredDomHash() {
        return storedDomHash;
    }

    public void setStoredDomHash(String storedDomHash) {
        this.storedDomHash = storedDomHash;
    }

    public String getLiveAnchor() {
        return liveAnchor;
    }

    public void setLiveAnchor(String liveAnchor) {
        this.liveAnchor = liveAnchor;
    }

    public String getLiveDomHash() {
        return liveDomHash;
    }

    public void setLiveDomHash(String liveDomHash) {
        this.liveDomHash = liveDomHash;
    }

    public String getLiveDomRelevantHtml() {
        return liveDomRelevantHtml;
    }

    public void setLiveDomRelevantHtml(String liveDomRelevantHtml) {
        this.liveDomRelevantHtml = liveDomRelevantHtml;
    }

    public String getHealeniumSuggestedXpath() {
        return healeniumSuggestedXpath;
    }

    public void setHealeniumSuggestedXpath(String healeniumSuggestedXpath) {
        this.healeniumSuggestedXpath = healeniumSuggestedXpath;
    }

    public Double getHealeniumScore() {
        return healeniumScore;
    }

    public String getHealeniumHintXpath() { return healeniumHintXpath; }

    public void setHealeniumHintXpath(String healeniumHintXpath) { this.healeniumHintXpath = healeniumHintXpath; }

    public void setHealeniumScore(Double healeniumScore) {
        this.healeniumScore = healeniumScore;
    }
}
