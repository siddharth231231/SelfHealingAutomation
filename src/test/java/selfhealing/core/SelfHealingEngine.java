package selfhealing.core;

import org.openqa.selenium.WebDriver;

import selfhealing.agent.AgentRestClient;
import selfhealing.dom.DomCaptureUtil;
import selfhealing.dom.DomContext;
import selfhealing.validation.ValidationEngine;
import selfhealing.validation.ValidationResult;

public class SelfHealingEngine {

    public static void heal(WebDriver driver, Throwable exception, String testName) {

        log("====================================");
        log("🔁 Self-Healing started for test: " + testName);
        log("====================================");

        /* -------------------------------------------------
         * 1️⃣ Basic safety checks
         * ------------------------------------------------- */
        if (driver == null || exception == null) {
            log("Invalid context (driver/exception null). Skipping healing.");
            return;
        }

        /* -------------------------------------------------
         * 2️⃣ Extract clean XPath
         * ------------------------------------------------- */
        String brokenXpath = LocatorExtractor.extract(exception);

        if (brokenXpath == null || brokenXpath.trim().isEmpty()) {
            log("Unable to extract clean XPath from exception.");
            return;
        }

        log("Extracted XPath only: [" + brokenXpath + "]");

        /* -------------------------------------------------
         * 3️⃣ Derive intent from XPath (CRITICAL)
         * ------------------------------------------------- */
        String expectedTag = extractTagFromXpath(brokenXpath);
        String expectedText = extractTextFromXpath(brokenXpath);

        log("Expected Tag  : " + expectedTag);
        log("Expected Text : " + expectedText);

        /* -------------------------------------------------
         * 4️⃣ Capture DOM context (intent-aware)
         * ------------------------------------------------- */
        DomContext domContext =
                DomCaptureUtil.capture(
                        driver,
                        brokenXpath,
                        expectedTag,
                        expectedText,
                        exception
                );

        if (domContext == null) {
            log("DOM context capture failed.");
            return;
        }

        log(domContext.toString());

        /* -------------------------------------------------
         * 5️⃣ Call AI Agent
         * ------------------------------------------------- */
        String healedXpath =
                AgentRestClient.requestHealing(domContext);

        if (healedXpath == null || healedXpath.trim().isEmpty()) {
            log("AI Agent failed to return healed XPath.");
            return;
        }

        log("AI suggested XPath: " + healedXpath);

        /* -------------------------------------------------
         * 6️⃣ Validation Engine (Safety Gate)
         * ------------------------------------------------- */
        ValidationResult validationResult =
                ValidationEngine.validate(driver, healedXpath);

        if (!validationResult.isValid()) {
            log("Validation failed: " + validationResult.getFailureReason());
            return;
        }

        /* -------------------------------------------------
         * 7️⃣ Healing success
         * ------------------------------------------------- */
        log("✅ Self-healing successful!");
        log("✅ Valid & unique XPath accepted: " + healedXpath);
    }

    /* =================================================
     * INTENT EXTRACTION HELPERS (SIMPLE & SAFE)
     * ================================================= */

    private static String extractTagFromXpath(String xpath) {
        // Examples:
        // //a[text()='Gmail'] -> a
        // //button[@id='submit'] -> button
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
            if (start == -1) return null;
            start += 8;
            int end = xpath.indexOf("'", start);
            return end > start ? xpath.substring(start, end) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /* -------------------------------------------------
     * Simple logger (intentional)
     * ------------------------------------------------- */
    private static void log(String message) {
        System.out.println("[SELF-HEALING] " + message);
    }
}
