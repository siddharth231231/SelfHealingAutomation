package selfhealing.core;

import org.openqa.selenium.WebDriver;

import selfhealing.agent.AgentRestClient;
import selfhealing.context.SelfHealingContext;
import selfhealing.context.SelfHealingContextBuilder;
import selfhealing.context.dom.DomCaptureUtil;
import selfhealing.context.dom.DomContext;
import selfhealing.context.storedContext.DbExtractedData;
import selfhealing.validation.ValidationEngine;
import selfhealing.validation.ValidationResult;

public class SelfHealingEngine {

    public static void heal(WebDriver driver, Throwable exception, String testName) {

        log("====================================");
        log("🔁 Self-Healing started for test: " + testName);
        log("====================================");

        /*
         * -------------------------------------------------
         * 1️⃣ Basic safety checks
         * -------------------------------------------------
         */
        if (driver == null || exception == null) {
            log("Invalid context (driver/exception null). Skipping healing.");
            return;
        }

        /*
         * -------------------------------------------------
         * 2️⃣ Extract clean XPath
         * -------------------------------------------------
         */
        String brokenXpath = LocatorExtractor.extract(exception);
        log(exception.getMessage());

        if (brokenXpath == null || brokenXpath.trim().isEmpty()) {
            log("Unable to extract clean XPath from exception.");
            return;
        }

        log("Extracted XPath only: [" + brokenXpath + "]");

        // Log any stored DB metadata that BasePage captured for this failure
        DbExtractedData stored = DbExtractedData.get();
        if (stored != null) {
            log("--- Stored DB Metadata ---");
            log("Locator Name        : " + stored.getLocatorName());
            log("Original Locator    : " + stored.getOriginalLocator());
            log("Current Active      : " + stored.getCurrentActiveLocator());
            log("Relative XPath      : " + stored.getRelativeXpath());
            log("Absolute XPath      : " + stored.getAbsoluteXpath());
            log("CSS Selector        : " + stored.getCssSelector());
            log("Parent XPath        : " + stored.getParentXpath());
            log("Parent XPath Chain  : " + stored.getParentXpathChain());
            log("Sibling XPaths      : " + stored.getSiblingXpaths());
            log("Sibling XPathCluster: " + stored.getSiblingXpathCluster());
            log("Page URL (stored)   : " + stored.getPageUrl());
            log("Page Title (stored) : " + stored.getPageTitle());
            log("--------------------------");
        } else {
            log("No DbExtractedData present for this failure.");
        }

        /*
         * -------------------------------------------------
         * 3️⃣ Derive intent from XPath (CRITICAL)
         * -------------------------------------------------
         */
        String expectedTag = extractTagFromXpath(brokenXpath);
        String expectedText = extractTextFromXpath(brokenXpath);

        log("Expected Tag  : " + expectedTag);
        log("Expected Text : " + expectedText);

        /*
         * -------------------------------------------------
         * 4️⃣ Capture DOM context using stored parent/sibling XPaths
         * -------------------------------------------------
         */
        DomContext domContext = DomCaptureUtil.capture(
                driver,
                stored,
                exception);

        // Propagate intent fields that the richer DomCaptureUtil doesn't set
        // automatically
        if (domContext != null) {
            domContext.setBrokenXpath(brokenXpath);
            domContext.setExpectedTag(expectedTag);
            domContext.setExpectedText(expectedText);
        }

        if (domContext == null) {
            log("DOM context capture failed.");
            return;
        }

        log(domContext.toString());

        /*
         * -------------------------------------------------
         * 5️⃣ Build unified context (live DOM + stored metadata)
         * -------------------------------------------------
         */
        SelfHealingContext healingContext = SelfHealingContextBuilder.build(testName, domContext, stored);

        log("Sending unified SelfHealingContext to Spring AI @ 8181...");

        /*
         * -------------------------------------------------
         * 6️⃣ Call AI Agent
         * -------------------------------------------------
         */
        String healedXpath = AgentRestClient.requestHealing(healingContext);

        if (healedXpath == null || healedXpath.trim().isEmpty()) {
            log("AI Agent failed to return healed XPath.");
            return;
        }

        log("AI suggested XPath: " + healedXpath);

        /*
         * -------------------------------------------------
         * 7️⃣ Validation Engine (Safety Gate)
         * -------------------------------------------------
         */
        ValidationResult validationResult = ValidationEngine.validate(driver, healedXpath);

        log("Validation Score: " + validationResult.getTotalScore() + "%");

        validationResult.getRuleScores()
                .forEach((rule, score) -> log(rule + " -> " + score + "%"));

        if (!validationResult.isValid()) {

            log("❌ XPath rejected (score below 80%)");
            return;
        }

        log("✅ XPath accepted (above 80%)");

        /*
         * -------------------------------------------------
         * 8️⃣ Healing success
         * -------------------------------------------------
         */
        log("✅ Self-healing successful!");
        log("✅ Valid & unique XPath accepted: " + healedXpath);
    }

    /*
     * =================================================
     * INTENT EXTRACTION HELPERS (SIMPLE & SAFE)
     * =================================================
     */

    private static String extractTagFromXpath(String xpath) {

        try {
            String cleaned = xpath.replaceAll("^//+", "");
            return cleaned.split("[\\[/]")[0];
        } catch (Exception e) {
            return "*";
        }
    }

    private static String extractTextFromXpath(String xpath) {
        // Extract text()='something'
        try {
            int start = xpath.indexOf("text()='");
            if (start == -1)
                return null;
            start += 8;
            int end = xpath.indexOf("'", start);
            return end > start ? xpath.substring(start, end) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /*
     * -------------------------------------------------
     * Simple logger (intentional)
     * -------------------------------------------------
     */
    private static void log(String message) {
        System.out.println("[SELF-HEALING] " + message);
    }
}
