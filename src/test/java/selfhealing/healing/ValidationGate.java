package selfhealing.healing;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ValidationGate {

    private static final int ATTRIBUTE_MATCH_THRESHOLD = 70;

    private ValidationGate() {
    }

    public static WebElement firstValid(
            WebDriver driver,
            List<String> rankedXpaths,
            String expectedTag) {
        ValidationTrace trace = validateWithTrace(driver, rankedXpaths, expectedTag);
        return trace.getSelectedElement();
    }

    public static String firstValidXpath(
            WebDriver driver,
            List<String> rankedXpaths,
            String expectedTag) {
        ValidationTrace trace = validateWithTrace(driver, rankedXpaths, expectedTag);
        return trace.getSelectedXpath();
    }

    public static ValidationTrace validateWithTrace(
            WebDriver driver,
            List<String> rankedXpaths,
            String expectedTag) {
        return validateWithTrace(driver, rankedXpaths, expectedTag, null, null);
    }

    public static ValidationTrace validateWithTrace(
            WebDriver driver,
            List<String> rankedXpaths,
            String expectedTag,
            String expectedText,
            String expectedDataTestId) {

        ValidationTrace trace = new ValidationTrace();
        if (driver == null || rankedXpaths == null) {
            trace.setSummary("driver/rankedXpaths is null");
            return trace;
        }

        for (String xpath : rankedXpaths) {
            ValidationCheck check = validateSingle(
                    driver,
                    xpath,
                    expectedTag,
                    expectedText,
                    expectedDataTestId
            );
            trace.getChecks().add(check);
            if (check.isPassed()) {
                trace.setSelectedXpath(check.getXpath());
                trace.setSelectedElement(check.getElement());
                trace.setSummary("first passing suggestion selected");
                break;
            }
        }

        if (trace.getSelectedXpath() == null) {
            trace.setSummary("no suggestion passed all validation rules");
        }
        return trace;
    }

    private static ValidationCheck validateSingle(
            WebDriver driver,
            String xpath,
            String expectedTag,
            String expectedText,
            String expectedDataTestId) {

        ValidationCheck check = new ValidationCheck();
        check.setXpath(xpath);
        check.setNotBlank(xpath != null && !xpath.trim().isEmpty());
        check.setRuleScores(new LinkedHashMap<>());
        check.setWeightedScorePercent(0);
        check.setValidationThresholdPercent(100);
        check.setAttributeMatchScore(0);
        check.setAttributeMatched(false);

        if (!check.isNotBlank()) {
            check.getRuleScores().put("Syntax (non-blank xpath)", 0);
            check.getRuleScores().put("Element found", 0);
            check.getRuleScores().put("Uniqueness", 0);
            check.getRuleScores().put("Tag match", 0);
            check.getRuleScores().put("Attribute match", 0);
            check.getRuleScores().put("Visibility", 0);
            check.getRuleScores().put("Enabled", 0);
            finalizeScores(check);
            check.setFailureReason("xpath is blank");
            return check;
        }
        check.getRuleScores().put("Syntax (non-blank xpath)", 100);

        List<WebElement> elements;
        try {
            elements = driver.findElements(By.xpath(xpath));
        } catch (Exception e) {
            check.getRuleScores().put("Element found", 0);
            check.getRuleScores().put("Uniqueness", 0);
            check.getRuleScores().put("Tag match", 0);
            check.getRuleScores().put("Attribute match", 0);
            check.getRuleScores().put("Visibility", 0);
            check.getRuleScores().put("Enabled", 0);
            finalizeScores(check);
            check.setFailureReason("xpath query failed: " + e.getClass().getSimpleName());
            return check;
        }

        check.setFoundCount(elements.size());
        check.setFound(elements.size() > 0);
        check.setUnique(elements.size() == 1);
        check.getRuleScores().put("Element found", check.isFound() ? 100 : 0);
        check.getRuleScores().put("Uniqueness", uniquenessScore(elements.size()));

        if (!check.isUnique()) {
            check.getRuleScores().put("Tag match", 0);
            check.getRuleScores().put("Attribute match", 0);
            check.getRuleScores().put("Visibility", 0);
            check.getRuleScores().put("Enabled", 0);
            finalizeScores(check);
            check.setFailureReason(elements.isEmpty()
                    ? "no elements matched"
                    : "xpath is not unique (matches=" + elements.size() + ")");
            return check;
        }

        WebElement element = elements.get(0);
        check.setElement(element);

        String liveTag = safeTag(element);
        check.setLiveTag(liveTag);
        boolean tagMatches = expectedTag == null || expectedTag.isBlank()
                || (liveTag != null && liveTag.equalsIgnoreCase(expectedTag));
        check.setTagMatches(tagMatches);
        check.getRuleScores().put("Tag match", tagMatches ? 100 : 0);

        int attributeMatchScore = computeAttributeMatchScore(element, expectedText, expectedDataTestId);
        boolean attributeMatched = attributeMatchScore >= ATTRIBUTE_MATCH_THRESHOLD;
        check.setAttributeMatchScore(attributeMatchScore);
        check.setAttributeMatched(attributeMatched);
        check.getRuleScores().put("Attribute match", attributeMatchScore);

        if (!tagMatches) {
            check.getRuleScores().put("Visibility", 0);
            check.getRuleScores().put("Enabled", 0);
            finalizeScores(check);
            check.setFailureReason("tag mismatch expected=" + expectedTag + " actual=" + liveTag);
            return check;
        }

        boolean visible = safeVisible(element);
        boolean enabled = safeEnabled(element);
        check.setVisible(visible);
        check.setEnabled(enabled);
        check.getRuleScores().put("Visibility", visible ? 100 : 0);
        check.getRuleScores().put("Enabled", enabled ? 100 : 0);
        finalizeScores(check);

        if (!visible || !enabled) {
            check.setFailureReason("visibility/enabled check failed (visible=" + visible + ", enabled=" + enabled + ")");
            return check;
        }

        check.setPassed(true);
        check.setFailureReason(null);
        return check;
    }

    private static String safeTag(WebElement element) {
        try {
            return element.getTagName();
        } catch (StaleElementReferenceException e) {
            return null;
        }
    }

    private static boolean safeVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private static boolean safeEnabled(WebElement element) {
        try {
            return element.isEnabled();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    private static String safeText(WebElement element) {
        try {
            String value = element.getText();
            return value == null ? null : value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String safeAttribute(WebElement element, String name) {
        try {
            return element.getAttribute(name);
        } catch (Exception e) {
            return null;
        }
    }

    private static int computeAttributeMatchScore(
            WebElement element,
            String expectedText,
            String expectedDataTestId) {
        if (element == null) {
            return 0;
        }

        List<Integer> parts = new ArrayList<>();

        if (expectedText != null && !expectedText.isBlank()) {
            String liveText = safeText(element);
            parts.add(textMatchScore(expectedText, liveText));
        }

        if (expectedDataTestId != null && !expectedDataTestId.isBlank()) {
            String liveDataTestId = safeAttribute(element, "data-testid");
            int score = expectedDataTestId.equalsIgnoreCase(
                    liveDataTestId == null ? "" : liveDataTestId
            ) ? 100 : 0;
            parts.add(score);
        }

        if (parts.isEmpty()) {
            return 100;
        }

        int sum = 0;
        for (Integer part : parts) {
            sum += part == null ? 0 : clamp(part, 0, 100);
        }
        return Math.round((float) sum / parts.size());
    }

    private static int textMatchScore(String expectedText, String liveText) {
        String expected = normalizeText(expectedText);
        String live = normalizeText(liveText);
        if (expected == null || live == null) {
            return 0;
        }
        if (expected.equals(live)) {
            return 100;
        }
        if (live.contains(expected) || expected.contains(live)) {
            return 75;
        }
        return 0;
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s+", " ").trim().toLowerCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private static int uniquenessScore(int matches) {
        if (matches == 1) {
            return 100;
        }
        if (matches > 1) {
            return 50;
        }
        return 0;
    }

    private static void finalizeScores(ValidationCheck check) {
        if (check == null) {
            return;
        }
        Map<String, Integer> scores = check.getRuleScores();
        if (scores == null) {
            scores = new LinkedHashMap<>();
            check.setRuleScores(scores);
        }

        int weighted = 0;
        int totalWeight = 0;

        weighted += weightedPart(scores, "Syntax (non-blank xpath)", 5);
        totalWeight += 5;

        weighted += weightedPart(scores, "Element found", 15);
        totalWeight += 15;

        weighted += weightedPart(scores, "Uniqueness", 20);
        totalWeight += 20;

        weighted += weightedPart(scores, "Tag match", 15);
        totalWeight += 15;

        weighted += weightedPart(scores, "Attribute match", 20);
        totalWeight += 20;

        weighted += weightedPart(scores, "Visibility", 15);
        totalWeight += 15;

        weighted += weightedPart(scores, "Enabled", 10);
        totalWeight += 10;

        check.setWeightedScorePercent(totalWeight == 0 ? 0 : Math.round((float) weighted / totalWeight));
    }

    private static int weightedPart(Map<String, Integer> scores, String key, int weight) {
        int score = 0;
        if (scores != null && scores.get(key) != null) {
            score = clamp(scores.get(key), 0, 100);
        }
        return score * weight;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static final class ValidationTrace {
        private final List<ValidationCheck> checks = new ArrayList<>();
        private String selectedXpath;
        private WebElement selectedElement;
        private String summary;

        public List<ValidationCheck> getChecks() {
            return checks;
        }

        public String getSelectedXpath() {
            return selectedXpath;
        }

        public void setSelectedXpath(String selectedXpath) {
            this.selectedXpath = selectedXpath;
        }

        public WebElement getSelectedElement() {
            return selectedElement;
        }

        public void setSelectedElement(WebElement selectedElement) {
            this.selectedElement = selectedElement;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }
    }

    public static final class ValidationCheck {
        private String xpath;
        private boolean notBlank;
        private boolean found;
        private int foundCount;
        private boolean unique;
        private String liveTag;
        private boolean tagMatches;
        private boolean visible;
        private boolean enabled;
        private boolean passed;
        private String failureReason;
        private WebElement element;
        private int weightedScorePercent;
        private int validationThresholdPercent;
        private Map<String, Integer> ruleScores = new LinkedHashMap<>();
        private int attributeMatchScore;
        private boolean attributeMatched;

        public String getXpath() {
            return xpath;
        }

        public void setXpath(String xpath) {
            this.xpath = xpath;
        }

        public boolean isNotBlank() {
            return notBlank;
        }

        public void setNotBlank(boolean notBlank) {
            this.notBlank = notBlank;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public int getFoundCount() {
            return foundCount;
        }

        public void setFoundCount(int foundCount) {
            this.foundCount = foundCount;
        }

        public boolean isUnique() {
            return unique;
        }

        public void setUnique(boolean unique) {
            this.unique = unique;
        }

        public String getLiveTag() {
            return liveTag;
        }

        public void setLiveTag(String liveTag) {
            this.liveTag = liveTag;
        }

        public boolean isTagMatches() {
            return tagMatches;
        }

        public void setTagMatches(boolean tagMatches) {
            this.tagMatches = tagMatches;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getFailureReason() {
            return failureReason;
        }

        public void setFailureReason(String failureReason) {
            this.failureReason = failureReason;
        }

        public WebElement getElement() {
            return element;
        }

        public void setElement(WebElement element) {
            this.element = element;
        }

        public int getWeightedScorePercent() {
            return weightedScorePercent;
        }

        public void setWeightedScorePercent(int weightedScorePercent) {
            this.weightedScorePercent = weightedScorePercent;
        }

        public int getValidationThresholdPercent() {
            return validationThresholdPercent;
        }

        public void setValidationThresholdPercent(int validationThresholdPercent) {
            this.validationThresholdPercent = validationThresholdPercent;
        }

        public Map<String, Integer> getRuleScores() {
            return ruleScores;
        }

        public void setRuleScores(Map<String, Integer> ruleScores) {
            this.ruleScores = ruleScores;
        }

        public int getAttributeMatchScore() {
            return attributeMatchScore;
        }

        public void setAttributeMatchScore(int attributeMatchScore) {
            this.attributeMatchScore = attributeMatchScore;
        }

        public boolean isAttributeMatched() {
            return attributeMatched;
        }

        public void setAttributeMatched(boolean attributeMatched) {
            this.attributeMatched = attributeMatched;
        }
    }
}
