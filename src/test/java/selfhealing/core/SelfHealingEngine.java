package selfhealing.core;

import com.yourcompany.selfhealing.entity.LocatorMetaEntity;
import com.yourcompany.selfhealing.service.LocatorMetaService;
import config.FrameworkConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import selfhealing.agent.AgentRestClient;
import selfhealing.agent.HealingRequest;
import selfhealing.healing.HealeniumAlgorithmService;
import selfhealing.healing.HealeniumSuggestion;
import selfhealing.healing.LiveDomData;
import selfhealing.healing.LiveDomExtractor;
import selfhealing.healing.ValidationGate;
import selfhealing.locator.NamedBy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SelfHealingEngine {

    private static final int SUGGESTION_PREVIEW_LIMIT = 12;

    private SelfHealingEngine() {
    }

    public static WebElement heal(
            WebDriver driver,
            NamedBy locator,
            LocatorMetaService locatorMetaService,
            Throwable failure) {

        if (driver == null || locator == null || locatorMetaService == null) {
            throw new HealingFailedException("Invalid healing context. Missing driver/locator/service.");
        }

        String pageUrl = PageUrlUtil.normalize(driver.getCurrentUrl());
        String locatorName = locator.getElementName();
        log("====================================================");
        log("Healing started. locator=" + locatorName + ", pageUrl=" + pageUrl);

        LocatorMetaEntity stored = locatorMetaService
                .findByPageUrlAndName(pageUrl, locatorName)
                .orElseThrow(() -> new LocatorNotCapturedException(
                        "No capture found for locatorName=" + locatorName + " pageUrl=" + pageUrl));
        logStoredSnapshot(stored);

        int maxAttempts = FrameworkConfig.getHealingRetryMax();
        String finalReason = "All healing attempts failed.";

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            log("---- Attempt " + attempt + "/" + maxAttempts + " ----");
            LiveDomData live = LiveDomExtractor.extract(driver, stored);
            logLiveSnapshot(live);

            // Temporarily disabled: Step-2 hash gate (DOM unchanged -> skip healing).
            // Keeping logs so we can re-enable later without losing observability.
            if (sameHash(stored.getDomHash(), live.getLiveNeighborhoodHash())) {
                log("Step-2 hash check matched, but skip gate is DISABLED temporarily. Continuing healing.");
            }

            HealeniumSuggestion healenium = HealeniumAlgorithmService.suggest(
                    stored.getNodePathJson(),
                    live.getCandidates()
            );
            log("Healenium suggestion: xpath=" + safe(healenium.getXpath()) +
                    ", score=" + healenium.getScore());

            HealingRequest request = buildRequest(
                    stored,
                    live,
                    healenium,
                    attempt,
                    maxAttempts,
                    locator
            );

            // Required behavior: call AI for every failure/attempt.
            List<String> aiSuggestions = AgentRestClient.requestHealing(
                    request,
                    FrameworkConfig.getHealingSpringAiUrl()
            );
            log("AI suggestions (" + aiSuggestions.size() + "): " + aiSuggestions);

            RankedSuggestions rankedSuggestions = mergeSuggestions(
                    aiSuggestions,
                    healenium.getXpath(),
                    stored,
                    live
            );
            logRankedSuggestions(rankedSuggestions);

            ValidationGate.ValidationTrace validationTrace = ValidationGate.validateWithTrace(
                    driver,
                    rankedSuggestions.getXpaths(),
                    stored.getElementTag(),
                    stored.getNormalizedVisibleText(),
                    stored.getDataTestId()
            );
            logValidationTrace(
                    validationTrace,
                    stored.getElementTag(),
                    rankedSuggestions.getSourceByXpath()
            );
            String selectedXpath = validationTrace.getSelectedXpath();

            if (selectedXpath != null) {
                WebElement healedElement = driver.findElement(By.xpath(selectedXpath));
                log("Selected healed xpath: " + selectedXpath);
                locatorMetaService.updateAfterHealing(
                        pageUrl,
                        locatorName,
                        selectedXpath,
                        live.getStructuralFingerprintJson(),
                        live.getLiveNeighborhoodHash(),
                        healenium.getScore()
                );
                locatorMetaService.recordHealingHistory(
                        locatorName,
                        pageUrl,
                        attempt,
                        "SUCCESS",
                        null,
                        aiSuggestions,
                        selectedXpath,
                        healenium.getXpath(),
                        healenium.getScore()
                );
                log("Healing SUCCESS on attempt " + attempt + ".");
                log("====================================================");
                return healedElement;
            }

            finalReason = "No AI/Healenium suggestion passed validation gate.";
            log("Attempt failed: " + finalReason);
            locatorMetaService.recordHealingHistory(
                    locatorName,
                    pageUrl,
                    attempt,
                    "FAILED",
                    finalReason,
                    aiSuggestions,
                    null,
                    healenium.getXpath(),
                    healenium.getScore()
            );
        }

        locatorMetaService.incrementHealingFailure(pageUrl, locatorName);
        log("Healing FAILED after max attempts. Reason=" + finalReason);
        log("====================================================");
        throw new HealingFailedException(finalReason, failure);
    }

    private static HealingRequest buildRequest(
            LocatorMetaEntity stored,
            LiveDomData live,
            HealeniumSuggestion healenium,
            int attempt,
            int maxAttempts,
            NamedBy brokenLocator) {
        HealingRequest request = new HealingRequest();
        request.setBrokenXpath(extractByValue(brokenLocator != null ? brokenLocator.getBy().toString() : null));
        request.setExpectedTag(stored.getElementTag());
        request.setExpectedText(stored.getNormalizedVisibleText());
        request.setLocatorName(stored.getLocatorName());
        request.setPageUrl(stored.getPageUrl());
        request.setAttempt(attempt);
        request.setMaxAttempts(maxAttempts);
        request.setStoredElementFingerprintJson(stored.getElementFingerprintJson());
        request.setStoredStructuralFingerprintJson(stored.getStructuralFingerprintJson());
        request.setStoredNodePathJson(stored.getNodePathJson());
        request.setStoredDomHash(stored.getDomHash());
        request.setLiveAnchor(live.getUsedAnchor());
        request.setLiveDomHash(live.getLiveNeighborhoodHash());
        request.setLiveDomRelevantHtml(live.getCleanedDom());
        request.setHealeniumSuggestedXpath(healenium.getXpath());
        request.setHealeniumScore(healenium.getScore());
        return request;
    }

    private static String extractByValue(String byAsString) {
        if (!notBlank(byAsString)) {
            return null;
        }
        int idx = byAsString.indexOf(":");
        if (idx >= 0 && idx + 1 < byAsString.length()) {
            return byAsString.substring(idx + 1).trim();
        }
        return byAsString.trim();
    }

    private static RankedSuggestions mergeSuggestions(
            List<String> aiSuggestions,
            String healeniumXpath,
            LocatorMetaEntity stored,
            LiveDomData live) {
        Set<String> unique = new LinkedHashSet<>();
        Map<String, String> sourceByXpath = new LinkedHashMap<>();
        if (aiSuggestions != null) {
            int rank = 1;
            for (String suggestion : aiSuggestions) {
                addSuggestion(unique, sourceByXpath, suggestion, "ai#" + rank);
                rank++;
            }
        }
        addSuggestion(unique, sourceByXpath, healeniumXpath, "healenium");

        if (stored != null) {
            addSuggestion(unique, sourceByXpath, stored.getCurrentActiveLocator(), "stored.currentActiveLocator");
            addSuggestion(unique, sourceByXpath, stored.getRelativeXpath(), "stored.relativeXpath");
            addSuggestion(unique, sourceByXpath, stored.getAbsoluteXpath(), "stored.absoluteXpath");
            if (notBlank(stored.getDataTestId())) {
                addSuggestion(
                        unique,
                        sourceByXpath,
                        "//*[@data-testid='" + escapeXpath(stored.getDataTestId()) + "']",
                        "stored.data-testid"
                );
            }
        }

        if (live != null && live.getCandidates() != null) {
            int count = 0;
            for (var candidate : live.getCandidates()) {
                if (count >= 30) {
                    break;
                }
                if (candidate != null && notBlank(candidate.getXpath())) {
                    addSuggestion(unique, sourceByXpath, candidate.getXpath(), "live.candidate");
                    count++;
                }
            }
        }

        return new RankedSuggestions(new ArrayList<>(unique), sourceByXpath);
    }

    private static void addSuggestion(
            Set<String> unique,
            Map<String, String> sourceByXpath,
            String suggestion,
            String source) {
        if (unique == null || !notBlank(suggestion)) {
            return;
        }
        String xpath = suggestion.trim();
        if (unique.add(xpath) && sourceByXpath != null) {
            sourceByXpath.put(xpath, source);
        }
    }

    private static String escapeXpath(String text) {
        return text == null ? "" : text.replace("'", "\\'");
    }

    private static boolean sameHash(String left, String right) {
        return left != null && right != null && left.equals(right);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static void logStoredSnapshot(LocatorMetaEntity stored) {
        if (stored == null) {
            log("Stored record: null");
            return;
        }
        log("Stored snapshot:");
        log(" locatorName           : " + safe(stored.getLocatorName()));
        log(" pageUrl               : " + safe(stored.getPageUrl()));
        log(" elementTag            : " + safe(stored.getElementTag()));
        log(" dataTestId            : " + safe(stored.getDataTestId()));
        log(" semanticPath          : " + safe(stored.getSemanticPath()));
        log(" domHash               : " + safe(stored.getDomHash()));
        log(" currentActiveLocator  : " + safe(stored.getCurrentActiveLocator()));
        log(" relativeXpath         : " + safe(stored.getRelativeXpath()));
        log(" absoluteXpath         : " + safe(stored.getAbsoluteXpath()));
        log(" cssSelector           : " + safe(stored.getCssSelector()));
        log(" tier1 fingerprint     : " + clip(stored.getElementFingerprintJson(), 600));
        log(" tier2 fingerprint     : " + clip(stored.getStructuralFingerprintJson(), 700));
        log(" tier3 nodePath        : " + clip(stored.getNodePathJson(), 700));
    }

    private static void logLiveSnapshot(LiveDomData live) {
        if (live == null) {
            log("Live DOM snapshot: null");
            return;
        }
        int candidateCount = live.getCandidates() == null ? 0 : live.getCandidates().size();
        log("Live DOM snapshot:");
        log(" usedAnchor            : " + safe(live.getUsedAnchor()));
        log(" neighborhoodHash      : " + safe(live.getLiveNeighborhoodHash()));
        log(" candidateCount        : " + candidateCount);
        log(" structuralFingerprint : " + clip(live.getStructuralFingerprintJson(), 700));
        log(" cleanedDom(len=" + lengthOf(live.getCleanedDom()) + "): " + clip(live.getCleanedDom(), 1000));
        if (candidateCount == 0) {
            log(" note                  : no live node candidates extracted for Healenium scoring.");
        }
    }

    private static void logRankedSuggestions(RankedSuggestions rankedSuggestions) {
        if (rankedSuggestions == null) {
            log("Ranked suggestions: null");
            return;
        }
        List<String> xpaths = rankedSuggestions.getXpaths();
        Map<String, String> sourceByXpath = rankedSuggestions.getSourceByXpath();
        int total = xpaths == null ? 0 : xpaths.size();
        int shown = Math.min(total, SUGGESTION_PREVIEW_LIMIT);

        log("Ranked suggestions: total=" + total + ", showing=" + shown);
        for (int i = 0; i < shown; i++) {
            String xpath = xpaths.get(i);
            log(" [" + (i + 1) + "] source=" + safe(sourceByXpath.get(xpath))
                    + " | xpath=" + clip(xpath, 240));
        }
        if (total > shown) {
            log(" ... " + (total - shown) + " more suggestion(s) not shown.");
        }
    }

    private static void logValidationTrace(
            ValidationGate.ValidationTrace trace,
            String expectedTag,
            Map<String, String> sourceByXpath) {
        if (trace == null) {
            log("Validation trace: null");
            return;
        }
        int totalChecked = trace.getChecks() == null ? 0 : trace.getChecks().size();
        log("Validation report:");
        log(" summary      : " + safe(trace.getSummary()));
        log(" expectedTag  : " + safe(expectedTag));
        log(" checkedCount : " + totalChecked);
        log(" selectedXpath: " + safe(trace.getSelectedXpath()));

        ValidationGate.ValidationCheck bestCheck = null;
        int i = 1;
        for (ValidationGate.ValidationCheck check : trace.getChecks()) {
            String xpath = check.getXpath();
            String source = sourceByXpath == null ? null : sourceByXpath.get(xpath);
            String status = check.isPassed() ? "PASS" : "FAIL";
            log(" [" + i + "] Candidate -> " + status + " | source=" + safe(source));
            log("      xpath        : " + clip(xpath, 240));
            log("      checkboxes   :");
            log("         " + checklistLine(
                    "Uniqueness",
                    ruleScore(check, "Uniqueness"),
                    check.isUnique(),
                    "PASS",
                    "FAIL"
            ));
            log("         " + checklistLine(
                    "Attribute Match",
                    check.getAttributeMatchScore(),
                    check.isAttributeMatched(),
                    "MATCH",
                    "MISMATCH"
            ));
            log("         " + checklistLine(
                    "Tag Match",
                    ruleScore(check, "Tag match"),
                    check.isTagMatches(),
                    "PASS",
                    "FAIL"
            ));
            log("         " + checklistLine(
                    "Visibility",
                    ruleScore(check, "Visibility"),
                    check.isVisible(),
                    "PASS",
                    "FAIL"
            ));
            log("         " + checklistLine(
                    "Enabled",
                    ruleScore(check, "Enabled"),
                    check.isEnabled(),
                    "PASS",
                    "FAIL"
            ));
            log("      finalScore   : " + check.getWeightedScorePercent() + "% (threshold="
                    + check.getValidationThresholdPercent() + "%)");
            if (check.getRuleScores() != null && !check.getRuleScores().isEmpty()) {
                log("      breakdown    : " + formatRuleScores(check.getRuleScores()));
            }
            if (!check.isPassed()) {
                log("      reason       : " + safe(check.getFailureReason()));
            }
            log("      finalValidation: " + (check.isPassed() ? "✅ PASS" : "❌ FAIL"));

            if (bestCheck == null || check.getWeightedScorePercent() > bestCheck.getWeightedScorePercent()) {
                bestCheck = check;
            }
            i++;
        }

        ValidationGate.ValidationCheck selectedCheck = findSelectedCheck(trace);
        if (selectedCheck != null) {
            String selectedSource = sourceByXpath == null ? null : sourceByXpath.get(selectedCheck.getXpath());
            log("Final Validation Result: ✅ PASS | source=" + safe(selectedSource)
                    + " | score=" + selectedCheck.getWeightedScorePercent() + "%");
        } else {
            int bestScore = bestCheck == null ? 0 : bestCheck.getWeightedScorePercent();
            log("Final Validation Result: ❌ FAIL | no candidate passed gate | bestScore=" + bestScore + "%");
        }
    }

    private static int lengthOf(String value) {
        return value == null ? 0 : value.length();
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "null";
        }
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max) + "...[truncated]";
    }

    private static String safe(String value) {
        return value == null ? "null" : value;
    }

    private static int ruleScore(ValidationGate.ValidationCheck check, String ruleName) {
        if (check == null || check.getRuleScores() == null || ruleName == null) {
            return 0;
        }
        Integer score = check.getRuleScores().get(ruleName);
        return score == null ? 0 : score;
    }

    private static String checklistLine(
            String label,
            int percent,
            boolean passed,
            String passWord,
            String failWord) {
        String icon = passed ? "✅ [x]" : "❌ [ ]";
        String status = passed ? passWord : failWord;
        return icon + " " + padRight(label, 16) + ": " + percent + "% " + status;
    }

    private static String padRight(String value, int width) {
        String text = value == null ? "" : value;
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    private static ValidationGate.ValidationCheck findSelectedCheck(ValidationGate.ValidationTrace trace) {
        if (trace == null || trace.getChecks() == null || trace.getSelectedXpath() == null) {
            return null;
        }
        for (ValidationGate.ValidationCheck check : trace.getChecks()) {
            if (check != null && trace.getSelectedXpath().equals(check.getXpath())) {
                return check;
            }
        }
        return null;
    }

    private static String formatRuleScores(Map<String, Integer> ruleScores) {
        if (ruleScores == null || ruleScores.isEmpty()) {
            return "{}";
        }
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Integer> entry : ruleScores.entrySet()) {
            if (!first) {
                out.append(", ");
            }
            out.append(entry.getKey()).append("=").append(entry.getValue()).append("%");
            first = false;
        }
        out.append("}");
        return out.toString();
    }

    private static void log(String message) {
        System.out.println("[SELF-HEALING] " + message);
    }

    private static final class RankedSuggestions {
        private final List<String> xpaths;
        private final Map<String, String> sourceByXpath;

        private RankedSuggestions(List<String> xpaths, Map<String, String> sourceByXpath) {
            this.xpaths = xpaths == null ? List.of() : xpaths;
            this.sourceByXpath = sourceByXpath == null ? Map.of() : sourceByXpath;
        }

        private List<String> getXpaths() {
            return xpaths;
        }

        private Map<String, String> getSourceByXpath() {
            return sourceByXpath;
        }
    }
}
